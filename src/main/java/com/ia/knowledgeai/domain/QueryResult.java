package com.ia.knowledgeai.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryResult {

	private final List<Source> results;

	private final String answer;

	private final List<Citation> sources;

	private final List<ContextSnippet> contextUsed;

	private final long latencyMs;

	private final Integer promptTokens;

	private final Integer completionTokens;

	public QueryResult(List<Source> results, long latencyMs) {
		this(results, null, Collections.emptyList(), Collections.emptyList(), latencyMs, null, null);
	}

	public QueryResult(List<Source> results, String answer, List<Citation> sources, List<ContextSnippet> contextUsed,
			long latencyMs, Integer promptTokens, Integer completionTokens) {
		this.results = results != null ? new ArrayList<>(results) : new ArrayList<>();
		this.answer = answer;
		this.sources = sources != null ? new ArrayList<>(sources) : new ArrayList<>();
		this.contextUsed = contextUsed != null ? new ArrayList<>(contextUsed) : new ArrayList<>();
		this.latencyMs = latencyMs;
		this.promptTokens = promptTokens;
		this.completionTokens = completionTokens;
	}

	public List<Source> getResults() {
		return Collections.unmodifiableList(results);
	}

	public String getAnswer() {
		return answer;
	}

	public List<Citation> getSources() {
		return Collections.unmodifiableList(sources);
	}

	public List<ContextSnippet> getContextUsed() {
		return Collections.unmodifiableList(contextUsed);
	}

	public long getLatencyMs() {
		return latencyMs;
	}

	public Integer getPromptTokens() {
		return promptTokens;
	}

	public Integer getCompletionTokens() {
		return completionTokens;
	}
}
