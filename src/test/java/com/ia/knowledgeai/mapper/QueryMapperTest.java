package com.ia.knowledgeai.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import com.ia.knowledgeai.domain.QueryResult;

class QueryMapperTest {

	private final QueryMapper queryMapper = new QueryMapper();

	@Test
	void shouldMapDocumentsToQueryResult() {
		UUID docId = UUID.randomUUID();
		Document document = new Document(
				"chunk content",
				Map.of(
						"documentId", docId.toString(),
						"title", "Sample",
						"chunkIndex", 1,
						"source", "manual",
						"tags", List.of("a", "b"),
						"score", 0.8));

		QueryResult result = queryMapper.toQueryResult(List.of(document), 15L);

		assertThat(result.getResults()).hasSize(1);
		assertThat(result.getResults().get(0).getDocumentId()).isEqualTo(docId);
		assertThat(result.getResults().get(0).getTitle()).isEqualTo("Sample");
		assertThat(result.getResults().get(0).getScore()).isEqualTo(0.8);
	}

	@Test
	void shouldMapQueryResultToResponse() {
		UUID docId = UUID.randomUUID();
		QueryResult result = new QueryResult(
				List.of(new com.ia.knowledgeai.domain.Source(docId, "Title", 0, "snippet", 0.5, "manual",
						List.of("a"))),
				10L);

		var response = queryMapper.toResponse(result);

		assertThat(response.results()).hasSize(1);
		assertThat(response.results().get(0).documentId()).isEqualTo(docId);
		assertThat(response.results().get(0).snippet()).isEqualTo("snippet");
	}
}
