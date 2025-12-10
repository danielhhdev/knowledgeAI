package com.ia.knowledgeai.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ia.knowledgeai.controller.impl.IngestControllerImpl;
import com.ia.knowledgeai.dto.request.IngestRequest;
import com.ia.knowledgeai.dto.response.IngestResponse;
import com.ia.knowledgeai.service.IngestService;

@WebMvcTest(controllers = IngestControllerImpl.class)
@Import(GlobalExceptionHandler.class)
class IngestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IngestService ingestService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void shouldReturnOkOnValidRequest() throws Exception {
		UUID documentId = UUID.randomUUID();
		when(ingestService.ingest(any())).thenReturn(new IngestResponse(documentId, 2, 20, "INGESTED"));

		IngestRequest request = new IngestRequest("source", "title", List.of("tag"), "hello world", null);

		mockMvc.perform(post("/api/ingest")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.documentId").value(documentId.toString()))
				.andExpect(jsonPath("$.status", is("INGESTED")));
	}

	@Test
	void shouldReturnBadRequestWhenMissingContent() throws Exception {
		IngestRequest request = new IngestRequest("source", "title", List.of(), null, null);
		Mockito.when(ingestService.ingest(any())).thenThrow(new IllegalArgumentException("Either text or url must be provided"));

		mockMvc.perform(post("/api/ingest")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}
}
