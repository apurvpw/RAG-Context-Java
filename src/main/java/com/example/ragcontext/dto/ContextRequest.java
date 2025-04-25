package com.example.ragcontext.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ContextRequest {
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Description is required")
    private String description;
} 