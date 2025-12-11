package com.ia.knowledgeai.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chunks")
public class Chunk {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "document_id", nullable = false)
	private Document document;

	@Column(name = "chunk_index", nullable = false)
	private int index;

	@Column(nullable = false, columnDefinition = "text")
	private String text;

	protected Chunk() {
		// JPA
	}

	public Chunk(UUID id, Document document, int index, String text) {
		this.id = id;
		this.document = document;
		this.index = index;
		this.text = text;
	}

	public UUID getId() {
		return id;
	}

	public Document getDocument() {
		return document;
	}

	public int getIndex() {
		return index;
	}

	public String getText() {
		return text;
	}
}
