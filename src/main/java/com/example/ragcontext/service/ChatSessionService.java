package com.example.ragcontext.service;

import com.example.ragcontext.dto.ChatMessageDto;
import com.example.ragcontext.dto.ChatSessionDto;
import com.example.ragcontext.dto.ChatSessionRequest;
import com.example.ragcontext.dto.SessionHistoryDto;
import com.example.ragcontext.model.ChatMessage;
import com.example.ragcontext.model.ChatSession;
import com.example.ragcontext.model.Context;
import com.example.ragcontext.repository.ChatMessageRepository;
import com.example.ragcontext.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ContextService contextService;

    public ChatSession createChatSession(ChatSessionRequest request) {
        // Verify the context exists
        Context context = contextService.getContextById(request.getContextId());
        
        ChatSession chatSession = ChatSession.builder()
                .contextId(request.getContextId())
                .contextName(request.getContextName())
                .sessionName(request.getSessionName() != null ? 
                        request.getSessionName() : 
                        "Session " + LocalDateTime.now().toString())
                .build();
        
        ChatSession savedSession = chatSessionRepository.save(chatSession);
        log.info("Created chat session with ID: {}", savedSession.getId());
        return savedSession;
    }

    public ChatSession getSessionById(String id) {
        return chatSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat session not found with ID: " + id));
    }
    
    public List<ChatSessionDto> getSessionsByContextId(String contextId) {
        return chatSessionRepository.findByContextIdOrderByLastActivityDesc(contextId)
                .stream()
                .map(ChatSessionDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ChatSessionDto> getAllSessions() {
        return chatSessionRepository.findAllByOrderByLastActivityDesc()
                .stream()
                .map(ChatSessionDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    public void saveUserMessage(String sessionId, String message) {
        // Update session last activity time
        ChatSession session = getSessionById(sessionId);
        session.setLastActivity(LocalDateTime.now());
        chatSessionRepository.save(session);
        
        // Save user message
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .type(ChatMessage.MessageType.USER)
                .content(message)
                .build();
        
        chatMessageRepository.save(chatMessage);
        log.info("Saved user message for session: {}", sessionId);
    }
    
    public void saveAiMessage(String sessionId, String message) {
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .type(ChatMessage.MessageType.AI)
                .content(message)
                .build();
        
        chatMessageRepository.save(chatMessage);
        log.info("Saved AI message for session: {}", sessionId);
    }
    
    public SessionHistoryDto getSessionHistory(String sessionId) {
        ChatSession session = getSessionById(sessionId);
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId);
        
        return SessionHistoryDto.builder()
                .session(ChatSessionDto.fromEntity(session))
                .messages(messages.stream().map(ChatMessageDto::fromEntity).collect(Collectors.toList()))
                .build();
    }
    
    public void updateSessionName(String sessionId, String sessionName) {
        ChatSession session = getSessionById(sessionId);
        session.setSessionName(sessionName);
        chatSessionRepository.save(session);
        log.info("Updated session name for session: {}", sessionId);
    }
} 