package com.ia.knowledgeai.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Citation {

	private final UUID documentId;

	private final String title;

	private final int chunkIndex;

	private final double score;

	private final String source;

	private final List<String> tags;

	public Citation(UUID documentId, String title, int chunkIndex, double score, String source, List<String> tags) {
		this.documentId = documentId;
		this.title = title;
		this.chunkIndex = chunkIndex;
		this.score = score;
		this.source = source;
		this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
	}

	public UUID getDocumentId() {
		return documentId;
	}

	public String getTitle() {
		return title;
	}

	public int getChunkIndex() {
		return chunkIndex;
	}

	public double getScore() {
		return score;
	}

	public String getSource() {
		return source;
	}

	public List<String> getTags() {
		return Collections.unmodifiableList(tags);
	}
}
