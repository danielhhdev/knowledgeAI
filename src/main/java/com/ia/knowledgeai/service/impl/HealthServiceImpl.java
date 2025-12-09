package com.ia.knowledgeai.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.ia.knowledgeai.dto.response.HealthResponse;
import com.ia.knowledgeai.service.HealthService;

@Service
public class HealthServiceImpl implements HealthService {

	@Override
	public HealthResponse getHealthStatus() {
		return new HealthResponse("UP", Instant.now());
	}
}
