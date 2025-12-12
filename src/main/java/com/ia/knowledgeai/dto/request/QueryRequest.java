package com.ia.knowledgeai.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QueryRequest(
		@NotBlank(message = "Query is required")
		@Size(max = 2000, message = "Query too long")
		String query,
		@Min(value = 1, message = "topK must be greater than 0")
		Integer topK,
		@DecimalMin(value = "0.0", inclusive = true, message = "similarityThreshold must be between 0 and 1")
		@DecimalMax(value = "1.0", inclusive = true, message = "similarityThreshold must be between 0 and 1")
		Double similarityThreshold,
		@Size(max = 100, message = "Source too long")
		String source,
		List<@Size(max = 50, message = "Tag too long") String> tags,
		UUID documentId,
		Boolean stream) {
}
