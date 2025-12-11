package com.ia.knowledgeai.domain.support;

import com.ia.knowledgeai.domain.ParsedDocument;

/**
 * Abstraction to convert binary documents into text suitable for chunking.
 */
public interface DocumentParser {

	ParsedDocument parse(byte[] content, String filename, String contentType);
}
