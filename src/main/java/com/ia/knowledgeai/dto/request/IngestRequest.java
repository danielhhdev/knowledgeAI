package com.ia.knowledgeai.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for document ingestion.
 */
public record IngestRequest(
		@NotBlank(message = "Source is required") @Size(max = 100, message = "Source too long") String source,
		@NotBlank(message = "Title is required") @Size(max = 255, message = "Title too long") String title,
		List<@Size(max = 50, message = "Tag too long") String> tags,
		String text,
		String url) {

	public boolean hasText() {
		return text != null && !text.isBlank();
	}

	public boolean hasUrl() {
		return url != null && !url.isBlank();
	}
}
