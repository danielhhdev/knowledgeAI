package com.ia.knowledgeai.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.ia.knowledgeai.config.QueryProperties;
import com.ia.knowledgeai.config.RagProperties;
import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.dto.request.QueryRequest;
import com.ia.knowledgeai.mapper.QueryMapper;
import com.ia.knowledgeai.service.QueryService;

import reactor.core.publisher.Flux;

@Service
public class QueryServiceImpl implements QueryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryServiceImpl.class);

	private final VectorStore vectorStore;

	private final QueryProperties queryProperties;

	private final RagProperties ragProperties;

	private final QueryMapper queryMapper;

	private final ChatModel chatModel;

	private final PromptTemplate ragPromptTemplate;

	public QueryServiceImpl(VectorStore vectorStore, QueryProperties queryProperties, RagProperties ragProperties,
			QueryMapper queryMapper, ChatModel chatModel, PromptTemplate ragPromptTemplate) {
		this.vectorStore = vectorStore;
		this.queryProperties = queryProperties;
		this.ragProperties = ragProperties;
		this.queryMapper = queryMapper;
		this.chatModel = chatModel;
		this.ragPromptTemplate = ragPromptTemplate;
	}

	@Override
	public QueryResult query(QueryRequest queryRequest) {
		validateQueryRequest(queryRequest);
		int topK = resolveTopK(queryRequest.topK(), queryProperties.getTopKDefault(), queryProperties.getTopKMax());
		double similarityThreshold = resolveThreshold(queryRequest.similarityThreshold(),
				queryProperties.getSimilarityThreshold());

		long start = Instant.now().toEpochMilli();
		try {
			List<Document> retrieved = retrieveDocuments(queryRequest, topK, similarityThreshold);
			List<Document> limited = limitResults(retrieved, topK);
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

	@Override
	public QueryResult answer(QueryRequest queryRequest) {
		validateQueryRequest(queryRequest);
		int topK = resolveTopK(queryRequest.topK(), ragProperties.getTopKDefault(), ragProperties.getTopKMax());
		double similarityThreshold = resolveThreshold(queryRequest.similarityThreshold(),
				ragProperties.getSimilarityThreshold());

		long start = Instant.now().toEpochMilli();
		List<Document> documents = retrieveDocuments(queryRequest, topK, similarityThreshold);
		if (documents.isEmpty()) {
			long latency = Instant.now().toEpochMilli() - start;
			LOGGER.info("No context found for query={}, returning empty answer", queryRequest.query());
			return queryMapper.toGenerativeResult(Collections.emptyList(), "", latency, null, null);
		}

		String context = buildContext(documents);
		Prompt prompt = ragPromptTemplate.create(Map.of("question", queryRequest.query(), "context", context));
		ChatResponse response = chatModel.call(prompt);
		String answer = response.getResult().getOutput().getText();
		Usage usage = response.getMetadata() != null ? response.getMetadata().getUsage() : null;
		Integer promptTokens = usage != null ? usage.getPromptTokens() : null;
		Integer completionTokens = usage != null ? usage.getCompletionTokens() : null;
		long latency = Instant.now().toEpochMilli() - start;
		return queryMapper.toGenerativeResult(documents, answer, latency, promptTokens, completionTokens);
	}

	@Override
	public Flux<String> streamAnswer(QueryRequest queryRequest) {
		validateQueryRequest(queryRequest);
		if (!ragProperties.isStreamingEnabled()) {
			return Flux.error(new IllegalStateException("Streaming is disabled by configuration"));
		}
		int topK = resolveTopK(queryRequest.topK(), ragProperties.getTopKDefault(), ragProperties.getTopKMax());
		double similarityThreshold = resolveThreshold(queryRequest.similarityThreshold(),
				ragProperties.getSimilarityThreshold());
		List<Document> documents = retrieveDocuments(queryRequest, topK, similarityThreshold);
		if (documents.isEmpty()) {
			return Flux.empty();
		}
		String context = buildContext(documents);
		Prompt prompt = ragPromptTemplate.create(Map.of("question", queryRequest.query(), "context", context));
		return chatModel.stream(prompt).map(chatResponse -> chatResponse.getResult().getOutput().getText());
	}

	private void validateQueryRequest(QueryRequest queryRequest) {
		if (queryRequest == null || !StringUtils.hasText(queryRequest.query())) {
			throw new IllegalArgumentException("Query must not be empty");
		}
	}

	private int resolveTopK(Integer requestedTopK, int defaultTopK, int maxTopK) {
		int value = requestedTopK != null ? requestedTopK : defaultTopK;
		if (value < 1) {
			value = 1;
		}
		if (value > maxTopK) {
			value = maxTopK;
		}
		return value;
	}

	private double resolveThreshold(Double threshold, double defaultThreshold) {
		double resolved = threshold != null ? threshold : defaultThreshold;
		if (resolved < 0.0) {
			return 0.0;
		}
		if (resolved > 1.0) {
			return 1.0;
		}
		return resolved;
	}

	private List<Document> retrieveDocuments(QueryRequest queryRequest, int topK, double similarityThreshold) {
		Filter.Expression filterExpression = buildFilterExpression(queryRequest);
		VectorStoreDocumentRetriever retriever = new VectorStoreDocumentRetriever(vectorStore, similarityThreshold, topK,
				() -> filterExpression);
		Query ragQuery = new Query(queryRequest.query());
		List<Document> retrieved = retriever.retrieve(ragQuery);
		return applyFilters(retrieved, queryRequest, similarityThreshold);
	}

	private Filter.Expression buildFilterExpression(QueryRequest queryRequest) {
		FilterExpressionBuilder builder = new FilterExpressionBuilder();
		FilterExpressionBuilder.Op filterExpression = null;
		if (queryRequest.documentId() != null) {
			filterExpression = builder.eq("documentId", queryRequest.documentId().toString());
		}
		if (StringUtils.hasText(queryRequest.source())) {
			FilterExpressionBuilder.Op sourceExpression = builder.eq("source", queryRequest.source());
			filterExpression = filterExpression != null ? builder.and(filterExpression, sourceExpression) : sourceExpression;
		}
		if (!CollectionUtils.isEmpty(queryRequest.tags())) {
			FilterExpressionBuilder.Op tagsExpression = builder.in("tags", new ArrayList<>(queryRequest.tags()));
			filterExpression = filterExpression != null ? builder.and(filterExpression, tagsExpression) : tagsExpression;
		}
		return filterExpression != null ? filterExpression.build() : null;
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

	private String buildContext(List<Document> documents) {
		StringBuilder contextBuilder = new StringBuilder();
		int maxDocs = Math.max(1, ragProperties.getMaxContextDocuments());
		int count = 0;
		for (Document document : documents) {
			if (count >= maxDocs) {
				break;
			}
			Map<String, Object> metadata = document.getMetadata();
			contextBuilder.append("DocumentId: ").append(metadata.getOrDefault("documentId", ""))
					.append(" | Title: ").append(metadata.getOrDefault("title", ""))
					.append(" | ChunkIndex: ").append(metadata.getOrDefault("chunkIndex", 0))
					.append(System.lineSeparator());
			contextBuilder.append(document.getText()).append(System.lineSeparator()).append("---")
					.append(System.lineSeparator());
			count++;
		}
		return contextBuilder.toString();
	}
}
