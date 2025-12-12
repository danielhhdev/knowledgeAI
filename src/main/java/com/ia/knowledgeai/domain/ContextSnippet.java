package com.ia.knowledgeai.domain;

import java.util.UUID;

public class ContextSnippet {

	private final UUID documentId;

	private final int chunkIndex;

	private final String snippet;

	public ContextSnippet(UUID documentId, int chunkIndex, String snippet) {
		this.documentId = documentId;
		this.chunkIndex = chunkIndex;
		this.snippet = snippet;
	}

	public UUID getDocumentId() {
		return documentId;
	}

	public int getChunkIndex() {
		return chunkIndex;
	}

	public String getSnippet() {
		return snippet;
	}
}
