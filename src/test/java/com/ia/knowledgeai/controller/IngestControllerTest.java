package com.ia.knowledgeai.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import com.ia.knowledgeai.controller.impl.IngestControllerImpl;
import com.ia.knowledgeai.dto.response.IngestResponse;
import com.ia.knowledgeai.service.IngestService;

@WebMvcTest(controllers = IngestControllerImpl.class)
@Import(GlobalExceptionHandler.class)
class IngestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private IngestService ingestService;

	@BeforeEach
	void setup() {
		org.mockito.MockitoAnnotations.openMocks(this);
	}

	@Test
	void shouldReturnOkOnValidRequest() throws Exception {
		UUID documentId = UUID.randomUUID();
		when(ingestService.ingest(any())).thenReturn(new IngestResponse(documentId, 2, 20, "INGESTED"));

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"sample.pdf",
				"application/pdf",
				"dummy content".getBytes());

		mockMvc.perform(multipart("/api/v1/documents/ingest")
				.file(file)
				.param("source", "source")
				.param("title", "title")
				.param("tags", "tag"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.documentId").value(documentId.toString()))
				.andExpect(jsonPath("$.status", is("INGESTED")));
	}

	@Test
	void shouldReturnBadRequestWhenMissingContent() throws Exception {
		MockMultipartFile emptyFile = new MockMultipartFile(
				"file",
				"sample.pdf",
				"application/pdf",
				new byte[0]);
		when(ingestService.ingest(any())).thenThrow(new IllegalArgumentException("A file must be provided for ingestion"));

		mockMvc.perform(multipart("/api/v1/documents/ingest")
				.file(emptyFile)
				.param("source", "source")
				.param("title", "title"))
				.andExpect(status().isBadRequest());
	}
}
