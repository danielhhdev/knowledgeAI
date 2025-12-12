package com.ia.knowledgeai.service;

import com.ia.knowledgeai.domain.QueryResult;
import com.ia.knowledgeai.dto.request.QueryRequest;

public interface QueryService {

	QueryResult query(QueryRequest queryRequest);
}
