package com.ia.knowledgeai.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ia.knowledgeai.config.QueryProperties;
import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.dto.request.QueryRequest;
import com.ia.knowledgeai.mapper.QueryMapper;
import com.ia.knowledgeai.service.QueryService;

@Service
public class QueryServiceImpl implements QueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryServiceImpl.class);

	private final VectorStore vectorStore;
	private final QueryProperties queryProperties;
	private final QueryMapper queryMapper;

	public QueryServiceImpl(VectorStore vectorStore, QueryProperties queryProperties, QueryMapper queryMapper) {
		this.vectorStore = vectorStore;
		this.queryProperties = queryProperties;
		this.queryMapper = queryMapper;
	}

	@Override
	public QueryResult query(QueryRequest queryRequest) {
		if (queryRequest == null || !StringUtils.hasText(queryRequest.query())) {
			throw new IllegalArgumentException("Query must not be empty");
		}
		int topK = resolveTopK(queryRequest.topK());
		double similarityThreshold = resolveThreshold(queryRequest.similarityThreshold());

		long start = Instant.now().toEpochMilli();
		try {
			List<Document> retrieved = vectorStore.similaritySearch(queryRequest.query());
			List<Document> filtered = applyFilters(retrieved, queryRequest, similarityThreshold);
			List<Document> limited = limitResults(filtered, topK);
			long latency = Instant.now().toEpochMilli() - start;
			LOGGER.info("Retrieved {} results for query (topK={}, source={} tags={})", limited.size(), topK,
					queryRequest.source(), queryRequest.tags());
			return queryMapper.toQueryResult(limited, latency);
		}
		catch (Exception ex) {
			LOGGER.error("Failed to retrieve results for query", ex);
			throw new IllegalStateException("Unable to retrieve documents", ex);
		}
	}

	private int resolveTopK(Integer topK) {
		int value = topK != null ? topK : queryProperties.getTopKDefault();
		if (value < 1) {
			value = 1;
		}
		if (value > queryProperties.getTopKMax()) {
			value = queryProperties.getTopKMax();
		}
		return value;
	}

	private double resolveThreshold(Double threshold) {
		double resolved = threshold != null ? threshold : queryProperties.getSimilarityThreshold();
		if (resolved < 0.0) {
			return 0.0;
		}
		if (resolved > 1.0) {
			return 1.0;
		}
		return resolved;
	}

	private List<Document> applyFilters(List<Document> documents, QueryRequest queryRequest, double threshold) {
		if (documents == null) {
			return List.of();
		}
		List<Document> filtered = new ArrayList<>();
		for (Document document : documents) {
			Map<String, Object> metadata = document.getMetadata();
			if (queryRequest.documentId() != null
					&& !queryRequest.documentId().toString().equals(String.valueOf(metadata.get("documentId")))) {
				continue;
			}
			if (StringUtils.hasText(queryRequest.source())) {
				Object source = metadata.get("source");
				if (source == null || !queryRequest.source().equals(source.toString())) {
					continue;
				}
			}
			if (queryRequest.tags() != null && !queryRequest.tags().isEmpty()) {
				Object tagsValue = metadata.get("tags");
				if (!(tagsValue instanceof List<?> existingTags) || !existingTags.containsAll(queryRequest.tags())) {
					continue;
				}
			}
			double score = parseScore(metadata);
			if (score > 0.0 && threshold > 0.0 && score < threshold) {
				continue;
			}
			filtered.add(document);
		}
		return filtered;
	}

	private List<Document> limitResults(List<Document> documents, int topK) {
		if (documents == null || documents.isEmpty()) {
			return List.of();
		}
		if (documents.size() <= topK) {
			return documents;
		}
		return documents.subList(0, topK);
	}

	private double parseScore(Map<String, Object> metadata) {
		Object scoreValue = metadata.get("score");
		if (scoreValue instanceof Number number) {
			return number.doubleValue();
		}
		if (scoreValue instanceof String text) {
			try {
				return Double.parseDouble(text);
			}
			catch (NumberFormatException ignored) {
				return 0.0;
			}
		}
		return 0.0;
	}
}
