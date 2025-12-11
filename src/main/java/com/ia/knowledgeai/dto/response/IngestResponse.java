package com.ia.knowledgeai.dto.response;

import java.util.UUID;

/**
 * Response for ingest operations.
 */
public record IngestResponse(UUID documentId, int chunksProcessed, int tokensCount, String status) {
}
