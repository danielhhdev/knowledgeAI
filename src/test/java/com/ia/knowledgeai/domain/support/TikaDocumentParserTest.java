package com.ia.knowledgeai.domain.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.ia.knowledgeai.domain.ParsedDocument;

class TikaDocumentParserTest {

	private final TikaDocumentParser parser = new TikaDocumentParser();

	@Test
	void shouldParsePlainText() {
		byte[] bytes = "Hello\nWorld   via   Tika".getBytes();

		ParsedDocument parsed = parser.parse(bytes, "sample.txt", "text/plain");

		assertThat(parsed.text()).isEqualTo("Hello World via Tika");
		assertThat(parsed.contentType()).contains("text");
	}

	@Test
	void shouldFailOnEmptyContent() {
		assertThatThrownBy(() -> parser.parse(new byte[0], "empty.pdf", "application/pdf"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
