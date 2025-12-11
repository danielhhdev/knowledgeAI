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
import org.springframework.mock.web.MockMultipartFile;

import com.ia.knowledgeai.config.IngestProperties;
import com.ia.knowledgeai.domain.Document;
import com.ia.knowledgeai.domain.ParsedDocument;
import com.ia.knowledgeai.dto.request.IngestRequest;
import com.ia.knowledgeai.dto.response.IngestResponse;
import com.ia.knowledgeai.domain.support.DocumentParser;
import com.ia.knowledgeai.repository.DocumentRepository;
import com.ia.knowledgeai.repository.VectorStoreRepository;
import com.ia.knowledgeai.domain.support.TextChunker;

@ExtendWith(MockitoExtension.class)
class IngestServiceImplTest {

	@Mock
	private DocumentRepository documentRepository;

	@Mock
	private VectorStoreRepository vectorStoreRepository;

	@Mock
	private VectorStore vectorStore;

	@Mock
	private DocumentParser documentParser;

	private TextChunker textChunker = new TextChunker();

	private IngestServiceImpl ingestService;

	@BeforeEach
	void setUp() {
		IngestProperties properties = new IngestProperties();
		properties.setChunkSize(8);
		properties.setChunkOverlap(2);
		properties.setMaxTextLength(5000);

		ingestService = new IngestServiceImpl(documentRepository, vectorStoreRepository, vectorStore, textChunker,
				documentParser, properties);
	}

	@Test
	void shouldIngestFileContent() {
		UUID documentId = UUID.randomUUID();
		when(documentRepository.save(any()))
				.thenReturn(new Document(documentId, "source", "title", List.of("tag"), Instant.now()));
		when(documentParser.parse(any(), any(), any()))
				.thenReturn(new ParsedDocument("sample text content", "application/pdf"));

		MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "pdf-content".getBytes());
		IngestRequest request = new IngestRequest("source", "title", List.of("tag1"), file);
		IngestResponse response = ingestService.ingest(request);

		assertThat(response.documentId()).isEqualTo(documentId);
		assertThat(response.status()).isEqualTo("INGESTED");
		verify(documentRepository).save(any());
		verify(vectorStoreRepository).saveAll(any());
		verify(vectorStore).add(any());
	}

	@Test
	void shouldFailWhenNoContent() {
		IngestRequest request = new IngestRequest("source", "title", List.of(), null);

		assertThatThrownBy(() -> ingestService.ingest(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("file must be provided for ingestion");
	}
}
