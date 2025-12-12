package com.ia.knowledgeai.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ia.knowledgeai.controller.QueryController;
import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.dto.request.QueryRequest;
import com.ia.knowledgeai.dto.response.QueryAnswerResponse;
import com.ia.knowledgeai.dto.response.QueryResponse;
import com.ia.knowledgeai.mapper.QueryMapper;
import com.ia.knowledgeai.service.QueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping
@Tag(name = "Query", description = "Endpoints for query retrieval and generation")
public class QueryControllerImpl implements QueryController {

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryControllerImpl.class);

	private final QueryService queryService;

	private final QueryMapper queryMapper;

	public QueryControllerImpl(QueryService queryService, QueryMapper queryMapper) {
		this.queryService = queryService;
		this.queryMapper = queryMapper;
	}

	@Override
	@PostMapping(path = { "/api/v1/query", "/api/v1/query/" }, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Retrieve relevant passages", description = "Returns the most relevant chunks for the provided query using vector search")
	public ResponseEntity<QueryResponse> query(@Valid @RequestBody QueryRequest queryRequest) {
		LOGGER.info("Received query request with source={} topK={} tags={}", queryRequest.source(), queryRequest.topK(),
				queryRequest.tags());
		try {
			QueryResult result = queryService.query(queryRequest);
			QueryResponse response = queryMapper.toResponse(result);
			return ResponseEntity.ok(response);
		}
		catch (IllegalArgumentException ex) {
			LOGGER.warn("Invalid query request: {}", ex.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}

	@Override
	@PostMapping(path = { "/api/v1/query/answer", "/api/v1/query/answer/" }, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Generate an answer using RAG", description = "Retrieves context and generates a natural language answer with citations")
	public ResponseEntity<QueryAnswerResponse> answer(@Valid @RequestBody QueryRequest queryRequest) {
		LOGGER.info("Received generative query with source={} topK={} tags={} stream={}", queryRequest.source(),
				queryRequest.topK(), queryRequest.tags(), queryRequest.stream());
		try {
			QueryResult result = queryService.answer(queryRequest);
			QueryAnswerResponse response = queryMapper.toAnswerResponse(result);
			return ResponseEntity.ok(response);
		}
		catch (IllegalArgumentException ex) {
			LOGGER.warn("Invalid generative query request: {}", ex.getMessage());
			return ResponseEntity.badRequest().build();
		}
	}

	@Override
	@PostMapping(path = { "/api/v1/query/answer/stream", "/api/v1/query/answer/stream/" }, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@Operation(summary = "Generate an answer using RAG (streaming)", description = "Streams the generated answer tokens using SSE")
	public SseEmitter streamAnswer(@Valid @RequestBody QueryRequest queryRequest) {
		LOGGER.info("Streaming generative query for source={} tags={}", queryRequest.source(), queryRequest.tags());
		SseEmitter emitter = new SseEmitter();
		Flux<String> stream = queryService.streamAnswer(queryRequest);
		stream.doOnError(emitter::completeWithError)
				.doOnComplete(emitter::complete)
				.subscribe(chunk -> {
					try {
						emitter.send(SseEmitter.event().data(chunk));
					}
					catch (Exception ex) {
						emitter.completeWithError(ex);
					}
				});
		return emitter;
	}
}
