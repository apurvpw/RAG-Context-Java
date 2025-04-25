package com.example.ragcontext.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage {
    
    @Id
    private String id;
    
    private String sessionId;
    
    @Builder.Default
    private MessageType type = MessageType.USER;
    
    private String content;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    public enum MessageType {
        USER,
        AI
    }
} 