package com.ia.knowledgeai.domain;

/**
 * Result of parsing a binary document into text plus metadata hints.
 */
public record ParsedDocument(String text, String contentType) {
}
