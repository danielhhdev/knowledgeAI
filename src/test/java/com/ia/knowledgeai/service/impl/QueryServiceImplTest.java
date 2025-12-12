package com.ia.knowledgeai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import com.ia.knowledgeai.config.QueryProperties;
import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.mapper.QueryMapper;

@ExtendWith(MockitoExtension.class)
class QueryServiceImplTest {

	@Mock
	private VectorStore vectorStore;

	private QueryServiceImpl queryService;

	@BeforeEach
	void setUp() {
		QueryProperties queryProperties = new QueryProperties();
		queryProperties.setTopKDefault(3);
		queryProperties.setTopKMax(5);
		queryProperties.setSimilarityThreshold(0.5);
		queryService = new QueryServiceImpl(vectorStore, queryProperties, new QueryMapper());
	}

	@Test
	void shouldClampTopKAndReturnResults() {
		UUID documentId = UUID.randomUUID();
		Document document = new Document("content snippet",
				Map.of("documentId", documentId.toString(), "title", "Doc", "chunkIndex", 2, "source", "wiki",
						"tags", List.of("tag1"), "score", 0.9));
		List<Document> docs = new ArrayList<>(List.of(document, document, document, document, document, document));
		when(vectorStore.similaritySearch(any(String.class))).thenReturn(docs);

		com.ia.knowledgeai.dto.request.QueryRequest request = new com.ia.knowledgeai.dto.request.QueryRequest(
				"test query", 10, 0.7, "wiki", List.of("tag1"), null);

		QueryResult result = queryService.query(request);

		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().get(0).getDocumentId()).isEqualTo(documentId);
		assertThat(result.getResults().get(0).getScore()).isEqualTo(0.9);

		verify(vectorStore, times(1)).similaritySearch("test query");
		assertThat(result.getResults()).hasSize(5); // clamped to max
	}

	@Test
	void shouldReturnEmptyResultsWhenRetrieverReturnsNone() {
		when(vectorStore.similaritySearch(any(String.class))).thenReturn(List.of());
		com.ia.knowledgeai.dto.request.QueryRequest request = new com.ia.knowledgeai.dto.request.QueryRequest(
				"empty query", null, null, null, List.of(), null);

		QueryResult result = queryService.query(request);

		assertThat(result.getResults()).isEmpty();
	}

	@Test
	void shouldValidateBlankQuery() {
		com.ia.knowledgeai.dto.request.QueryRequest request = new com.ia.knowledgeai.dto.request.QueryRequest(
				" ", null, null, null, null, null);
		assertThatThrownBy(() -> queryService.query(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Query must not be empty");
	}
}
