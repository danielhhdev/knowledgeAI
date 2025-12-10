package com.ia.knowledgeai.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.ia.knowledgeai.config.IngestProperties;
import com.ia.knowledgeai.domain.Chunk;
import com.ia.knowledgeai.domain.Document;
import com.ia.knowledgeai.dto.request.IngestRequest;
import com.ia.knowledgeai.dto.response.IngestResponse;
import com.ia.knowledgeai.repository.DocumentRepository;
import com.ia.knowledgeai.repository.VectorStoreRepository;
import com.ia.knowledgeai.service.IngestService;
import com.ia.knowledgeai.service.TextChunker;

@Service
public class IngestServiceImpl implements IngestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(IngestServiceImpl.class);
	private static final String STATUS_INGESTED = "INGESTED";

	private final DocumentRepository documentRepository;
	private final VectorStoreRepository vectorStoreRepository;
	private final VectorStore vectorStore;
	private final TextChunker textChunker;
	private final IngestProperties ingestProperties;
	private final RestClient restClient;

	public IngestServiceImpl(DocumentRepository documentRepository,
			VectorStoreRepository vectorStoreRepository,
			VectorStore vectorStore,
			TextChunker textChunker,
			IngestProperties ingestProperties,
			RestClient restClient) {
		this.documentRepository = documentRepository;
		this.vectorStoreRepository = vectorStoreRepository;
		this.vectorStore = vectorStore;
		this.textChunker = textChunker;
		this.ingestProperties = ingestProperties;
		this.restClient = restClient;
	}

	@Override
	@Transactional
	public IngestResponse ingest(IngestRequest ingestRequest) {
		validateRequest(ingestRequest);
		String content = resolveContent(ingestRequest);
		validateLength(content);

		Document document = saveDocument(ingestRequest);
		List<String> chunkTexts = textChunker.chunk(content, ingestProperties.getChunkSize(),
				ingestProperties.getChunkOverlap());
		List<Chunk> persistedChunks = persistChunks(document, chunkTexts);
		storeEmbeddings(document, persistedChunks);

		int tokensCount = content.length();
		return new IngestResponse(document.getId(), persistedChunks.size(), tokensCount, STATUS_INGESTED);
	}

	private void validateRequest(IngestRequest ingestRequest) {
		if (ingestRequest == null) {
			throw new IllegalArgumentException("Ingest request must not be null");
		}
		boolean hasText = ingestRequest.hasText();
		boolean hasUrl = ingestRequest.hasUrl();
		if (!hasText && !hasUrl) {
			throw new IllegalArgumentException("Either text or url must be provided");
		}
		if (hasText && hasUrl) {
			throw new IllegalArgumentException("Provide only one of text or url");
		}
	}

	private String resolveContent(IngestRequest ingestRequest) {
		if (ingestRequest.hasText()) {
			return ingestRequest.text().trim();
		}
		try {
			return restClient.get()
					.uri(ingestRequest.url())
					.retrieve()
					.body(String.class);
		}
		catch (RestClientException ex) {
			throw new IllegalArgumentException("Unable to fetch content from url", ex);
		}
	}

	private void validateLength(String content) {
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("Content cannot be empty");
		}
		if (content.length() > ingestProperties.getMaxTextLength()) {
			throw new IllegalArgumentException("Content exceeds maximum length of " + ingestProperties.getMaxTextLength());
		}
	}

	private Document saveDocument(IngestRequest ingestRequest) {
		Document document = new Document(UUID.randomUUID(), ingestRequest.source(), ingestRequest.title(),
				ingestRequest.tags(), Instant.now());
		return documentRepository.save(document);
	}

	private List<Chunk> persistChunks(Document document, List<String> chunkTexts) {
		List<Chunk> chunks = new ArrayList<>();
		int index = 0;
		for (String chunkText : chunkTexts) {
			if (chunkText == null || chunkText.isBlank()) {
				continue;
			}
			chunks.add(new Chunk(UUID.randomUUID(), document, index++, chunkText));
		}
		if (!chunks.isEmpty()) {
			vectorStoreRepository.saveAll(chunks);
		}
		return chunks;
	}

	private void storeEmbeddings(Document document, List<Chunk> chunks) {
		if (chunks.isEmpty()) {
			LOGGER.warn("No chunks generated for document {}", document.getId());
			return;
		}
		List<org.springframework.ai.document.Document> vectorDocuments = new ArrayList<>();
		for (Chunk chunk : chunks) {
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("documentId", document.getId().toString());
			metadata.put("chunkIndex", chunk.getIndex());
			metadata.put("source", document.getSource());
			metadata.put("title", document.getTitle());
			metadata.put("tags", document.getTags());
			vectorDocuments.add(new org.springframework.ai.document.Document(chunk.getText(), metadata));
		}
		vectorStore.add(vectorDocuments);
		LOGGER.info("Stored {} chunks for document {}", chunks.size(), document.getId());
	}
}
