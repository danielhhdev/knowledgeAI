package com.ia.knowledgeai.service;

import com.ia.knowledgeai.dto.request.IngestRequest;
import com.ia.knowledgeai.dto.response.IngestResponse;

public interface IngestService {

	IngestResponse ingest(IngestRequest ingestRequest);
}
