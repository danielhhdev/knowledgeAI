package com.ia.knowledgeai.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "documents")
public class Document {

	@Id
	private UUID id;

	@Column(nullable = false, length = 100)
	private String source;

	@Column(nullable = false, length = 255)
	private String title;

	@ElementCollection
	@CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
	@Column(name = "tag", length = 50)
	private List<String> tags = new ArrayList<>();

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected Document() {
		// JPA
	}

	public Document(UUID id, String source, String title, List<String> tags, Instant createdAt) {
		this.id = id;
		this.source = source;
		this.title = title;
		if (tags != null) {
			this.tags = new ArrayList<>(tags);
		}
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public String getSource() {
		return source;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getTags() {
		return tags;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
