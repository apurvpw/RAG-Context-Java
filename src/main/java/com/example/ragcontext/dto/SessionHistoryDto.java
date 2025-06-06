package com.example.ragcontext.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionHistoryDto {
    private ChatSessionDto session;
    private List<ChatMessageDto> messages;
} 