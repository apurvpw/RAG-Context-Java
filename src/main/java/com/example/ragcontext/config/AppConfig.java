package com.example.ragcontext.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private final AppProperties appProperties;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public EmbeddingModel embeddingModel() {
        logger.info("Initializing Ollama embedding model with URL: {}, model: {}", 
                appProperties.getOllama().getUrl(), 
                appProperties.getOllama().getEmbeddingModel());
        
        return OllamaEmbeddingModel.builder()
                .baseUrl(appProperties.getOllama().getUrl())
                .modelName(appProperties.getOllama().getEmbeddingModel())
                .build();
    }

    @Bean
    public ChromaEmbeddingStore chromaEmbeddingStore() {
        logger.info("Initializing ChromaEmbeddingStore with URL: {}", appProperties.getChroma().getUrl());
        return ChromaEmbeddingStore.builder()
                .baseUrl(appProperties.getChroma().getUrl())
                .build();
    }
}