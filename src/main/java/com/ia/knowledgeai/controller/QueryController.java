package com.ia.knowledgeai.controller;

import org.springframework.http.ResponseEntity;

import com.ia.knowledgeai.dto.request.QueryRequest;
import com.ia.knowledgeai.dto.response.QueryAnswerResponse;
import com.ia.knowledgeai.dto.response.QueryResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Contract for query endpoints.
 */
public interface QueryController {

	ResponseEntity<QueryResponse> query(QueryRequest queryRequest);

	ResponseEntity<QueryAnswerResponse> answer(QueryRequest queryRequest);

	SseEmitter streamAnswer(QueryRequest queryRequest);
}
