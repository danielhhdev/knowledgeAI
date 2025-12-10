package com.ia.knowledgeai.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class TextChunkerTest {

	private final TextChunker textChunker = new TextChunker();

	@Test
	void shouldChunkWithOverlap() {
		String content = "abcdefghijklmnopqrstuvwxyz";
		List<String> chunks = textChunker.chunk(content, 10, 3);

		assertThat(chunks).hasSize(4);
		assertThat(chunks.get(0)).isEqualTo("abcdefghij");
		assertThat(chunks.get(1)).isEqualTo("hijklmnopq");
		assertThat(chunks.get(2)).isEqualTo("opqrstuvwx");
		assertThat(chunks.get(3)).isEqualTo("vwxyz");
	}

	@Test
	void shouldReturnEmptyListWhenTextBlank() {
		List<String> chunks = textChunker.chunk("   ", 10, 2);
		assertThat(chunks).isEmpty();
	}
}
