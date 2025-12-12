package com.ia.knowledgeai.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ia.knowledgeai.controller.impl.QueryControllerImpl;
import com.ia.knowledgeai.dto.request.QueryRequest;
import com.ia.knowledgeai.mapper.QueryMapper;
import com.ia.knowledgeai.service.impl.QueryServiceImpl;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.mockito.Mockito;

@WebMvcTest(controllers = QueryControllerImpl.class)
@Import({ QueryMapper.class, QueryServiceImpl.class, GlobalExceptionHandler.class })
class QueryControllerIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private VectorStore vectorStore;

	@Test
	void shouldReturnOrderedResultsFromRetriever() throws Exception {
		UUID docId = UUID.randomUUID();
		Document doc = new Document("snippet", Map.of(
				"documentId", docId.toString(),
				"title", "Guide",
				"chunkIndex", 0,
				"source", "wiki",
				"tags", List.of("tag1"),
				"score", 0.75));
		when(vectorStore.similaritySearch(anyString())).thenReturn(List.of(doc));

		QueryRequest request = new QueryRequest("query content", 2, 0.5, "wiki", List.of("tag1"), null);

		mockMvc.perform(post("/api/v1/query")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.results", hasSize(1)))
				.andExpect(jsonPath("$.results[0].documentId", is(docId.toString())))
				.andExpect(jsonPath("$.results[0].score", is(0.75)));
	}

	@Test
	void shouldHandleNoResultsGracefully() throws Exception {
		when(vectorStore.similaritySearch(anyString())).thenReturn(List.of());

		QueryRequest request = new QueryRequest("no match", null, null, null, null, null);

		mockMvc.perform(post("/api/v1/query")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.results", hasSize(0)));
	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		VectorStore vectorStore() {
			return Mockito.mock(VectorStore.class);
		}
	}
}
