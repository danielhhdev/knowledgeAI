package com.ia.knowledgeai.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;

import com.ia.knowledgeai.config.QueryProperties;
import com.ia.knowledgeai.config.RagProperties;
import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.mapper.QueryMapper;

@ExtendWith(MockitoExtension.class)
class QueryServiceImplTest {

	@Mock
	private VectorStore vectorStore;

	@Mock
	private ChatModel chatModel;

	@Mock
	private PromptTemplate promptTemplate;

	private QueryServiceImpl queryService;

	@BeforeEach
	void setUp() {
		QueryProperties queryProperties = new QueryProperties();
		queryProperties.setTopKDefault(3);
		queryProperties.setTopKMax(5);
		queryProperties.setSimilarityThreshold(0.5);

		RagProperties ragProperties = new RagProperties();
		ragProperties.setTopKDefault(5);
		ragProperties.setTopKMax(8);
		ragProperties.setSimilarityThreshold(0.4);

		queryService = new QueryServiceImpl(vectorStore, queryProperties, ragProperties, new QueryMapper(), chatModel,
				promptTemplate);
	}

	@Test
	void shouldClampTopKAndReturnResults() {
		UUID documentId = UUID.randomUUID();
		Document document = new Document("content snippet",
				Map.of("documentId", documentId.toString(), "title", "Doc", "chunkIndex", 2, "source", "wiki",
						"tags", List.of("tag1"), "score", 0.9));
		List<Document> docs = new ArrayList<>(List.of(document, document, document, document, document, document));
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(docs);

		com.ia.knowledgeai.dto.request.QueryRequest request = new com.ia.knowledgeai.dto.request.QueryRequest(
				"test query", 10, 0.7, "wiki", List.of("tag1"), null, null);

		QueryResult result = queryService.query(request);

		assertThat(result.getResults()).hasSize(5); // clamped to max
		assertThat(result.getResults().get(0).getDocumentId()).isEqualTo(documentId);
		assertThat(result.getResults().get(0).getScore()).isEqualTo(0.9);

		verify(vectorStore, times(1)).similaritySearch(any(SearchRequest.class));
	}

	@Test
	void shouldReturnEmptyResultsWhenRetrieverReturnsNone() {
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());
		com.ia.knowledgeai.dto.request.QueryRequest request = new com.ia.knowledgeai.dto.request.QueryRequest(
				"empty query", null, null, null, List.of(), null, null);

		QueryResult result = queryService.query(request);

		assertThat(result.getResults()).isEmpty();
	}

	@Test
	void shouldValidateBlankQuery() {
		com.ia.knowledgeai.dto.request.QueryRequest request = new com.ia.knowledgeai.dto.request.QueryRequest(
				" ", null, null, null, null, null, null);
		assertThatThrownBy(() -> queryService.query(request))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Query must not be empty");
	}

	@Test
	void shouldGenerateAnswerUsingChatModel() {
		UUID documentId = UUID.randomUUID();
		Document document = new Document("context text",
				Map.of("documentId", documentId.toString(), "title", "Doc", "chunkIndex", 1, "score", 0.9));
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(document));
		when(promptTemplate.create(any(Map.class))).thenReturn(new Prompt("prompt"));
		ChatResponse chatResponse = new ChatResponse(List.of(new Generation(new AssistantMessage("final answer"))));
		when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);

		com.ia.knowledgeai.dto.request.QueryRequest request = new com.ia.knowledgeai.dto.request.QueryRequest(
				"query", null, null, null, null, null, false);

		QueryResult result = queryService.answer(request);

		assertThat(result.getAnswer()).isEqualTo("final answer");
		assertThat(result.getSources()).hasSize(1);
		assertThat(result.getContextUsed()).hasSize(1);
		verify(chatModel, times(1)).call(any(Prompt.class));
	}
}
