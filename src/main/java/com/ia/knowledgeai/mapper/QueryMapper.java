package com.ia.knowledgeai.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ia.knowledgeai.domain.Citation;
import com.ia.knowledgeai.domain.ContextSnippet;
import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.domain.Source;
import com.ia.knowledgeai.dto.response.QueryAnswerResponse;
import com.ia.knowledgeai.dto.response.QueryResponse;

@Component
public class QueryMapper {

	public QueryResult toQueryResult(List<Document> documents, long latencyMs) {
		if (documents == null || documents.isEmpty()) {
			return new QueryResult(Collections.emptyList(), latencyMs);
		}
		List<Source> sources = mapSources(documents);
		return new QueryResult(sources, latencyMs);
	}

	public QueryResult toGenerativeResult(List<Document> documents, String answer, long latencyMs,
			Integer promptTokens, Integer completionTokens) {
		List<Source> sources = mapSources(documents);
		List<Citation> citations = mapCitations(documents);
		List<ContextSnippet> contextUsed = mapContextSnippets(documents);
		return new QueryResult(sources, answer, citations, contextUsed, latencyMs, promptTokens, completionTokens);
	}

	public QueryResponse toResponse(QueryResult queryResult) {
		List<QueryResponse.QueryResultItem> responseItems = new ArrayList<>();
		for (Source source : queryResult.getResults()) {
			responseItems.add(new QueryResponse.QueryResultItem(
					source.getDocumentId(),
					source.getTitle(),
					source.getChunkIndex(),
					source.getSnippet(),
					source.getScore(),
					source.getSource(),
					source.getTags()));
		}
		return new QueryResponse(responseItems);
	}

	public QueryAnswerResponse toAnswerResponse(QueryResult queryResult) {
		List<QueryAnswerResponse.SourceCitation> citationItems = new ArrayList<>();
		for (Citation citation : queryResult.getSources()) {
			citationItems.add(new QueryAnswerResponse.SourceCitation(
					citation.getDocumentId(),
					citation.getTitle(),
					citation.getChunkIndex(),
					citation.getScore(),
					citation.getSource(),
					citation.getTags()));
		}

		List<QueryAnswerResponse.ContextSnippet> contextItems = new ArrayList<>();
		for (ContextSnippet contextSnippet : queryResult.getContextUsed()) {
			contextItems.add(new QueryAnswerResponse.ContextSnippet(
					contextSnippet.getDocumentId(),
					contextSnippet.getChunkIndex(),
					contextSnippet.getSnippet()));
		}

		return new QueryAnswerResponse(
				queryResult.getAnswer(),
				citationItems,
				contextItems,
				queryResult.getLatencyMs(),
				queryResult.getPromptTokens(),
				queryResult.getCompletionTokens());
	}

	private List<Source> mapSources(List<Document> documents) {
		List<Source> sources = new ArrayList<>();
		for (Document document : documents) {
			Map<String, Object> metadata = document.getMetadata();
			UUID documentId = parseUuid(metadata.get("documentId"));
			String title = parseString(metadata.get("title"));
			int chunkIndex = parseInt(metadata.get("chunkIndex"));
			String source = parseString(metadata.get("source"));
			List<String> tags = parseTags(metadata.get("tags"));
			double score = parseDouble(metadata.get("score"), metadata.get("distance"));
			String snippet = parseSnippet(metadata);

			sources.add(new Source(documentId, title, chunkIndex, snippet, score, source, tags));
		}
		return sources;
	}

	private List<Citation> mapCitations(List<Document> documents) {
		List<Citation> citations = new ArrayList<>();
		for (Document document : documents) {
			Map<String, Object> metadata = document.getMetadata();
			UUID documentId = parseUuid(metadata.get("documentId"));
			String title = parseString(metadata.get("title"));
			int chunkIndex = parseInt(metadata.get("chunkIndex"));
			String source = parseString(metadata.get("source"));
			List<String> tags = parseTags(metadata.get("tags"));
			double score = parseDouble(metadata.get("score"), metadata.get("distance"));
			citations.add(new Citation(documentId, title, chunkIndex, score, source, tags));
		}
		return citations;
	}

	private List<ContextSnippet> mapContextSnippets(List<Document> documents) {
		List<ContextSnippet> contextSnippets = new ArrayList<>();
		for (Document document : documents) {
			Map<String, Object> metadata = document.getMetadata();
			UUID documentId = parseUuid(metadata.get("documentId"));
			int chunkIndex = parseInt(metadata.get("chunkIndex"));
			String snippet = parseSnippet(metadata);
			contextSnippets.add(new ContextSnippet(documentId, chunkIndex, snippet));
		}
		return contextSnippets;
	}

	private List<String> parseTags(Object tagsObject) {
		if (tagsObject instanceof List<?> list) {
			List<String> tags = new ArrayList<>();
			for (Object tag : list) {
				if (tag != null) {
					tags.add(tag.toString());
				}
			}
			return tags;
		}
		return Collections.emptyList();
	}

	private UUID parseUuid(Object value) {
		if (value instanceof UUID uuid) {
			return uuid;
		}
		if (value instanceof String text && StringUtils.hasText(text)) {
			try {
				return UUID.fromString(text);
			}
			catch (IllegalArgumentException ignored) {
				return null;
			}
		}
		return null;
	}

	private String parseString(Object value) {
		return value != null ? value.toString() : null;
	}

	private int parseInt(Object value) {
		if (value instanceof Number number) {
			return number.intValue();
		}
		if (value instanceof String text && StringUtils.hasText(text)) {
			try {
				return Integer.parseInt(text);
			}
			catch (NumberFormatException ignored) {
				return 0;
			}
		}
		return 0;
	}

	private double parseDouble(Object primary, Object fallback) {
		Object target = primary != null ? primary : fallback;
		if (target instanceof Number number) {
			return number.doubleValue();
		}
		if (target instanceof String text && StringUtils.hasText(text)) {
			try {
				return Double.parseDouble(text);
			}
			catch (NumberFormatException ignored) {
				return 0.0;
			}
		}
		return 0.0;
	}

	private String parseSnippet(Map<String, Object> metadata) {
		String snippet = parseString(metadata.get("content"));
		if (!StringUtils.hasText(snippet)) {
			snippet = parseString(metadata.get("text"));
		}
		return snippet != null ? snippet : "";
	}
}
