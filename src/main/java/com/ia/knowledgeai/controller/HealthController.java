package com.ia.knowledgeai.controller;

import org.springframework.http.ResponseEntity;

import com.ia.knowledgeai.dto.response.HealthResponse;

public interface HealthController {

	ResponseEntity<HealthResponse> getHealth();
}
