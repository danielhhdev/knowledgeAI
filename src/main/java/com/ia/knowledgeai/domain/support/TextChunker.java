package com.ia.knowledgeai.domain.support;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

/**
 * Token-based chunker with overlap using Spring AI TokenTextSplitter.
 */
@Component
public class TextChunker {

	public List<String> chunk(String text, int chunkSize, int overlap) {
		if (text == null || text.isBlank()) {
			return List.of();
		}
		int size = Math.max(1, chunkSize);
		int effectiveOverlap = Math.max(0, Math.min(overlap, size));
		// chunkSize, minChunkSize, maxChunkSize, overlap, keepSeparator
		TokenTextSplitter splitter = new TokenTextSplitter(size, size, size, effectiveOverlap, false);
		List<Document> docs = splitter.split(new Document(text));
		return docs.stream().map(Document::getText).toList();
	}
}
