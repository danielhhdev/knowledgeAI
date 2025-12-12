package com.ia.knowledgeai.dto.response;

import java.util.List;
import java.util.UUID;

public record QueryAnswerResponse(
		String answer,
		List<SourceCitation> sources,
		List<ContextSnippet> contextUsed,
		long latencyMs,
		Integer promptTokens,
		Integer completionTokens) {

	public record SourceCitation(
			UUID documentId,
			String title,
			int chunkIndex,
			double score,
			String source,
			List<String> tags) {
	}

	public record ContextSnippet(
			UUID documentId,
			int chunkIndex,
			String snippet) {
	}
}
