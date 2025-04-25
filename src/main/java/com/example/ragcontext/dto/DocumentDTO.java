package com.example.ragcontext.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private String id;
    private String contextId;
    private String filename;
    private String contentType;
    private long fileSize;
    
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
} 