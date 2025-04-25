package com.example.ragcontext.dto;

import com.example.ragcontext.model.ChatSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionDto {
    private String id;
    private String contextId;
    private String contextName;
    private String sessionName;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
    
    public static ChatSessionDto fromEntity(ChatSession entity) {
        return ChatSessionDto.builder()
                .id(entity.getId())
                .contextId(entity.getContextId())
                .contextName(entity.getContextName())
                .sessionName(entity.getSessionName())
                .createdAt(entity.getCreatedAt())
                .lastActivity(entity.getLastActivity())
                .build();
    }
} 