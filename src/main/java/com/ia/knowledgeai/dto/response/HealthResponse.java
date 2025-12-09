package com.ia.knowledgeai.dto.response;

import java.time.Instant;

public record HealthResponse(String status, Instant timestamp) {
}
