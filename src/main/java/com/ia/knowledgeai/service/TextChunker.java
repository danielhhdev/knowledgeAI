package com.ia.knowledgeai.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Simple character-based chunker with overlap.
 */
@Component
public class TextChunker {

	public List<String> chunk(String text, int chunkSize, int overlap) {
		List<String> chunks = new ArrayList<>();
		if (text == null || text.isBlank()) {
			return chunks;
		}
		int start = 0;
		int length = text.length();
		int effectiveOverlap = Math.max(0, Math.min(overlap, chunkSize));
		while (start < length) {
			int end = Math.min(start + chunkSize, length);
			chunks.add(text.substring(start, end));
			if (end == length) {
				break;
			}
			start = end - effectiveOverlap;
		}
		return chunks;
	}
}
