package com.ia.knowledgeai.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ia.knowledgeai.domain.support.TextChunker;

class TextChunkerTest {

	private final TextChunker textChunker = new TextChunker();

	@Test
	void shouldChunkWithOverlap() {
		String content = "one two three four five six";
		List<String> chunks = textChunker.chunk(content, 3, 1);

		assertThat(chunks).hasSize(3);
		assertThat(chunks.get(0)).isEqualTo("one two three");
		assertThat(chunks.get(1)).isEqualTo("three four five");
		assertThat(chunks.get(2)).isEqualTo("five six");
	}

	@Test
	void shouldReturnEmptyListWhenTextBlank() {
		List<String> chunks = textChunker.chunk("   ", 10, 2);
		assertThat(chunks).isEmpty();
	}
}
