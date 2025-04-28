package com.example.ragcontext.config;

import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OllamaConfig {

    private final AppProperties appProperties;

    @Bean
    public OllamaChatModel ollamaChatModel() {
        return OllamaChatModel.builder()
                .baseUrl(appProperties.getOllama().getUrl())
                .modelName(appProperties.getOllama().getChatModel())
                .temperature(0.7)
                .build();
    }
} 