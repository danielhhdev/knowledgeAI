package com.ia.knowledgeai.domain.support;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ia.knowledgeai.domain.ParsedDocument;

/**
 * Apache Tika based parser for PDF/DOCX and other supported formats.
 */
@Component
public class TikaDocumentParser implements DocumentParser {

	private static final Logger LOGGER = LoggerFactory.getLogger(TikaDocumentParser.class);
	private final Tika tika = new Tika();

	@Override
	public ParsedDocument parse(byte[] content, String filename, String contentType) {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("File content is empty");
		}
		Metadata metadata = new Metadata();
		if (StringUtils.hasText(filename)) {
			metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filename);
		}
		if (StringUtils.hasText(contentType)) {
			metadata.set(Metadata.CONTENT_TYPE, contentType);
		}
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(content)) {
			String parsedText = tika.parseToString(inputStream);
			String detectedType = StringUtils.hasText(contentType) ? contentType : tika.detect(content, filename);
			String normalized = normalize(parsedText);
			if (!StringUtils.hasText(normalized)) {
				throw new IllegalArgumentException("Parsed document has no extractable text (is it empty or scanned?)");
			}
			LOGGER.debug("Parsed file {} with detected content type {}", filename, detectedType);
			return new ParsedDocument(normalized, detectedType);
		}
		catch (IOException | TikaException ex) {
			throw new IllegalArgumentException("Unable to parse document content", ex);
		}
	}

	private String normalize(String parsedText) {
		if (parsedText == null) {
			return "";
		}
		return parsedText.replaceAll("\\s+", " ").trim();
	}
}
