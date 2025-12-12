package com.ia.knowledgeai.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ia.knowledgeai.controller.impl.QueryControllerImpl;
import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.domain.Source;
import com.ia.knowledgeai.dto.request.QueryRequest;
import com.ia.knowledgeai.mapper.QueryMapper;
import com.ia.knowledgeai.service.QueryService;

@WebMvcTest(controllers = QueryControllerImpl.class)
@Import({ QueryMapper.class, GlobalExceptionHandler.class })
class QueryControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@org.mockito.Mock
	private QueryService queryService;

	@BeforeEach
	void setup() {
		org.mockito.MockitoAnnotations.openMocks(this);
	}

	@Test
	void shouldReturnResultsOnValidRequest() throws Exception {
		UUID docId = UUID.randomUUID();
		QueryResult result = new QueryResult(
				List.of(new Source(docId, "Title", 1, "snippet", 0.9, "wiki", List.of("tag1"))),
				20L);
		when(queryService.query(any(QueryRequest.class))).thenReturn(result);

		QueryRequest request = new QueryRequest("how to", 3, 0.5, "wiki", List.of("tag1"), null);

		mockMvc.perform(post("/api/v1/query")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.results", hasSize(1)))
				.andExpect(jsonPath("$.results[0].documentId", is(docId.toString())))
				.andExpect(jsonPath("$.results[0].score", is(0.9)));
	}

	@Test
	void shouldReturnBadRequestWhenMissingQuery() throws Exception {
		QueryRequest request = new QueryRequest(" ", null, null, null, null, null);
		when(queryService.query(any(QueryRequest.class)))
				.thenThrow(new IllegalArgumentException("Query must not be empty"));

		mockMvc.perform(post("/api/v1/query")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}
}
