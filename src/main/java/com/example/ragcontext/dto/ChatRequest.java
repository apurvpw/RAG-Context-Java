package com.example.ragcontext.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    private String contextId;
    
    @NotBlank(message = "Context name is required")
    private String contextName;
    
    @NotBlank(message = "Question is required")
    private String question;
} 