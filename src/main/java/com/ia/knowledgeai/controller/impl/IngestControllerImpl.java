package com.ia.knowledgeai.controller.impl;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ia.knowledgeai.controller.IngestController;
import com.ia.knowledgeai.dto.request.IngestRequest;
import com.ia.knowledgeai.dto.response.IngestResponse;
import com.ia.knowledgeai.service.IngestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping
@Tag(name = "Ingest", description = "Endpoints for document ingestion")
public class IngestControllerImpl implements IngestController {

	private static final Logger LOGGER = LoggerFactory.getLogger(IngestControllerImpl.class);

	private final IngestService ingestService;

	public IngestControllerImpl(IngestService ingestService) {
		this.ingestService = ingestService;
	}

	@Override
	@PostMapping(path = { "/api/v1/documents/ingest", "/api/v1/documents/ingest/" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Ingest a document", description = "Chunk, embed and persist a document into the knowledge base")
	public ResponseEntity<IngestResponse> ingest(@Valid @ModelAttribute IngestRequest ingestRequest) {
		LOGGER.info("Received ingest request for source={} title={} filename={} at {}", ingestRequest.getSource(),
				ingestRequest.getTitle(), ingestRequest.getFile() != null ? ingestRequest.getFile().getOriginalFilename() : "none",
				Instant.now());
		IngestResponse response = ingestService.ingest(ingestRequest);
		return ResponseEntity.ok(response);
	}
}
