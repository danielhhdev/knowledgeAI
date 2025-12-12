package com.ia.knowledgeai.dto.response;

import java.util.List;
import java.util.UUID;

public record QueryResponse(List<QueryResultItem> results) {

	public record QueryResultItem(
			UUID documentId,
			String title,
			int chunkIndex,
			String snippet,
			double score,
			String source,
			List<String> tags) {
	}
}
