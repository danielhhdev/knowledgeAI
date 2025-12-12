package com.ia.knowledgeai.controller.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ia.knowledgeai.controller.QueryController;
import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.dto.request.QueryRequest;
import com.ia.knowledgeai.dto.response.QueryResponse;
import com.ia.knowledgeai.mapper.QueryMapper;
import com.ia.knowledgeai.service.QueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping
@Tag(name = "Query", description = "Endpoints for query retrieval")
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
		QueryResult result = queryService.query(queryRequest);
		QueryResponse response = queryMapper.toResponse(result);
		return ResponseEntity.ok(response);
	}
}
