package com.example.ragcontext.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
@EnableConfigurationProperties
public class AppProperties {

    private ChromaConfig chroma = new ChromaConfig();
    private OllamaConfig ollama = new OllamaConfig();
    private EmbeddingConfig embedding = new EmbeddingConfig();
    private DocumentConfig document = new DocumentConfig();
    private WebSocketConfig websocket = new WebSocketConfig();

    @Data
    public static class ChromaConfig {
        private String url;
    }

    @Data
    public static class OllamaConfig {
        private String url;
        private String model;
        private String chatModel;
        private String embeddingModel;
        
        // For backward compatibility
        public String getChatModel() {
            return chatModel != null ? chatModel : model;
        }
        
        public String getEmbeddingModel() {
            return embeddingModel != null ? embeddingModel : model;
        }
    }

    @Data
    public static class EmbeddingConfig {
        private int dimensions;
    }

    @Data
    public static class DocumentConfig {
        private int chunkSize;
        private int chunkOverlap;
    }

    @Data
    public static class WebSocketConfig {
        private String endpoint;
        private String destinationPrefix;
    }
} 