package com.example.ragcontext.controller;

import com.example.ragcontext.dto.ChatRequest;
import com.example.ragcontext.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat")
    public void handleChatMessage(@Valid ChatRequest request) {
        log.info("Received chat message for session: {}", request.getSessionId());
        chatService.processStreamingChatRequest(request);
    }
} 