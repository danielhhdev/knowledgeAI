package com.ia.knowledgeai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.client.RestClient;

import com.ia.knowledgeai.config.IngestProperties;
import com.ia.knowledgeai.domain.Document;
import com.ia.knowledgeai.dto.request.IngestRequest;
import com.ia.knowledgeai.dto.response.IngestResponse;
import com.ia.knowledgeai.repository.DocumentRepository;
import com.ia.knowledgeai.repository.VectorStoreRepository;
import com.ia.knowledgeai.service.TextChunker;

@ExtendWith(MockitoExtension.class)
class IngestServiceImplTest {

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private VectorStoreRepository vectorStoreRepository;

	@Mock
	private VectorStore vectorStore;

	private TextChunker textChunker = new TextChunker();

	private IngestServiceImpl ingestService;

	@BeforeEach
	void setUp() {
		IngestProperties properties = new IngestProperties();
		properties.setChunkSize(8);
		properties.setChunkOverlap(2);
		properties.setMaxTextLength(5000);

		RestClient restClient = RestClient.builder().build();
		ingestService = new IngestServiceImpl(documentRepository, vectorStoreRepository, vectorStore, textChunker,
				properties, restClient);
	}

	@Test
	void shouldIngestTextContent() {
		UUID documentId = UUID.randomUUID();
		when(documentRepository.save(any()))
				.thenReturn(new Document(documentId, "source", "title", List.of("tag"), Instant.now()));

		IngestRequest request = new IngestRequest("source", "title", List.of("tag1"), "sample text content", null);
		IngestResponse response = ingestService.ingest(request);

		assertThat(response.documentId()).isEqualTo(documentId);
		assertThat(response.status()).isEqualTo("INGESTED");
		verify(documentRepository).save(any());
		verify(vectorStoreRepository).saveAll(any());
		verify(vectorStore).add(any());
	}

	@Test
	void shouldFailWhenNoContent() {
		IngestRequest request = new IngestRequest("source", "title", List.of(), null, null);

		assertThatThrownBy(() -> ingestService.ingest(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Either text or url must be provided");
	}
}
