package com.ia.knowledgeai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.ai.chat.prompt.PromptTemplate;

@Configuration
@EnableConfigurationProperties({ IngestProperties.class, QueryProperties.class, RagProperties.class })
public class SpringAIConfig {

	@Bean
	public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
		return PgVectorStore.builder(jdbcTemplate, embeddingModel).build();
	}

	@Bean
	public RestClient restClient() {
		return RestClient.create();
	}

	@Bean
	public PromptTemplate ragPromptTemplate(RagProperties ragProperties, ResourceLoader resourceLoader) {
		Resource promptResource = resourceLoader.getResource(ragProperties.getPromptTemplate());
		return new PromptTemplate(promptResource);
	}

	@Bean
	public ChatClient chatClient(ChatModel chatModel) {
		return ChatClient.builder(chatModel).build();
	}
}
