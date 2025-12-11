package com.ia.knowledgeai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ingest")
public class IngestProperties {

	private int chunkSize = 800;

	private int chunkOverlap = 200;

	private int maxTextLength = 20000;

	private long maxFileSizeBytes = 10 * 1024 * 1024;

	private java.util.List<String> allowedContentTypes = java.util.List.of(
			"application/pdf",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document");

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public int getChunkOverlap() {
		return chunkOverlap;
	}

	public void setChunkOverlap(int chunkOverlap) {
		this.chunkOverlap = chunkOverlap;
	}

	public int getMaxTextLength() {
		return maxTextLength;
	}

	public void setMaxTextLength(int maxTextLength) {
		this.maxTextLength = maxTextLength;
	}

	public long getMaxFileSizeBytes() {
		return maxFileSizeBytes;
	}

	public void setMaxFileSizeBytes(long maxFileSizeBytes) {
		this.maxFileSizeBytes = maxFileSizeBytes;
	}

	public java.util.List<String> getAllowedContentTypes() {
		return allowedContentTypes;
	}

	public void setAllowedContentTypes(java.util.List<String> allowedContentTypes) {
		if (allowedContentTypes == null) {
			return;
		}
		this.allowedContentTypes = allowedContentTypes;
	}
}
