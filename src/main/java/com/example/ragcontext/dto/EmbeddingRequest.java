package com.example.ragcontext.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {
    
    private String contextId; // No longer required as it will be generated
    
    @NotBlank(message = "Context name is required")
    private String contextName;
    
    private String contextDescription;
    
    // One of these should be provided
    private MultipartFile document;
    private String documentUrl;
    
    public boolean hasDocument() {
        return document != null && !document.isEmpty();
    }
    
    public boolean hasDocumentUrl() {
        return documentUrl != null && !documentUrl.trim().isEmpty();
    }
} 