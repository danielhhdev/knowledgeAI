package com.ia.knowledgeai.service;

import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.dto.request.QueryRequest;

import reactor.core.publisher.Flux;

public interface QueryService {

	QueryResult query(QueryRequest queryRequest);

	QueryResult answer(QueryRequest queryRequest);

	Flux<String> streamAnswer(QueryRequest queryRequest);
}
