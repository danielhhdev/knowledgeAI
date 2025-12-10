package com.ia.knowledgeai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import com.ia.knowledgeai.dto.request.IngestRequest;
import com.ia.knowledgeai.dto.response.IngestResponse;

import jakarta.validation.Valid;

/**
 * Contract for ingest endpoints.
 */
@Validated
public interface IngestController {

	ResponseEntity<IngestResponse> ingest(@Valid IngestRequest ingestRequest);
}
