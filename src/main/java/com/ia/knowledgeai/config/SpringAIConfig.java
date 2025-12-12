package com.ia.knowledgeai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ IngestProperties.class, QueryProperties.class })
public class SpringAIConfig {

	@Bean
	public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
		return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
	}

	@Bean
	public RestClient restClient() {
		return RestClient.create();
	}
}
