package com.example.ragcontext.dto;

import com.example.ragcontext.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String id;
    private String content;
    private String type; // "USER" or "AI"
    private LocalDateTime timestamp;
    
    public static ChatMessageDto fromEntity(ChatMessage entity) {
        return ChatMessageDto.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .type(entity.getType().name())
                .timestamp(entity.getTimestamp())
                .build();
    }
} 