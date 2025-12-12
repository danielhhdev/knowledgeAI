package com.ia.knowledgeai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "query")
public class QueryProperties {

	private int topKDefault = 5;

	private int topKMax = 10;

	private double similarityThreshold = 0.6;

	private String mode = "semantic";

	public int getTopKDefault() {
		return topKDefault;
	}

	public void setTopKDefault(int topKDefault) {
		this.topKDefault = topKDefault;
	}

	public int getTopKMax() {
		return topKMax;
	}

	public void setTopKMax(int topKMax) {
		this.topKMax = topKMax;
	}

	public double getSimilarityThreshold() {
		return similarityThreshold;
	}

	public void setSimilarityThreshold(double similarityThreshold) {
		this.similarityThreshold = similarityThreshold;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
}
