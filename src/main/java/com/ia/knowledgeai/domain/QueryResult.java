package com.ia.knowledgeai.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueryResult {

	private final List<Source> results;

	private final long latencyMs;

	public QueryResult(List<Source> results, long latencyMs) {
		this.results = results != null ? new ArrayList<>(results) : new ArrayList<>();
		this.latencyMs = latencyMs;
	}

	public List<Source> getResults() {
		return Collections.unmodifiableList(results);
	}

	public long getLatencyMs() {
		return latencyMs;
	}
}
