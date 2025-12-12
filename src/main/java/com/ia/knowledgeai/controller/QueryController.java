package com.ia.knowledgeai.controller;

import org.springframework.http.ResponseEntity;

import com.ia.knowledgeai.dto.request.QueryRequest;
import com.ia.knowledgeai.dto.response.QueryResponse;

/**
 * Contract for query endpoints.
 */
public interface QueryController {

	ResponseEntity<QueryResponse> query(QueryRequest queryRequest);
}
