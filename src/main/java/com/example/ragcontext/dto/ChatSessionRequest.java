package com.example.ragcontext.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatSessionRequest {
    
    private String contextId;
    
    @NotBlank(message = "Context name is required")
    private String contextName;
    
    private String contextDescription;
    
    private String sessionName;
} 