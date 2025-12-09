package com.ia.knowledgeai.controller.impl;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ia.knowledgeai.controller.HealthController;
import com.ia.knowledgeai.dto.response.HealthResponse;
import com.ia.knowledgeai.service.HealthService;

@RestController
@RequestMapping("/api/health")
public class HealthControllerImpl implements HealthController {

	private static final Logger LOGGER = LoggerFactory.getLogger(HealthControllerImpl.class);

	private final HealthService healthService;

	public HealthControllerImpl(HealthService healthService) {
		this.healthService = healthService;
	}

	@Override
	@GetMapping
	public ResponseEntity<HealthResponse> getHealth() {
		LOGGER.info("Received health check at {}", Instant.now());
		return ResponseEntity.ok(healthService.getHealthStatus());
	}
}
