package com.ia.knowledgeai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public class RagProperties {

	private String model = "llama3.2";

	private int topKDefault = 5;

	private int topKMax = 10;

	private double similarityThreshold = 0.6;

	private String promptTemplate = "classpath:prompts/rag-answer.st";

	private boolean streamingEnabled = true;

	private int maxContextDocuments = 8;

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

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

	public String getPromptTemplate() {
		return promptTemplate;
	}

	public void setPromptTemplate(String promptTemplate) {
		this.promptTemplate = promptTemplate;
	}

	public boolean isStreamingEnabled() {
		return streamingEnabled;
	}

	public void setStreamingEnabled(boolean streamingEnabled) {
		this.streamingEnabled = streamingEnabled;
	}

	public int getMaxContextDocuments() {
		return maxContextDocuments;
	}

	public void setMaxContextDocuments(int maxContextDocuments) {
		this.maxContextDocuments = maxContextDocuments;
	}
}
