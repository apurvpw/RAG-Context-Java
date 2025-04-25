package com.example.ragcontext.controller;

import com.example.ragcontext.dto.ChatSessionDto;
import com.example.ragcontext.dto.ChatSessionRequest;
import com.example.ragcontext.dto.SessionHistoryDto;
import com.example.ragcontext.model.ChatSession;
import com.example.ragcontext.model.Context;
import com.example.ragcontext.service.ChatSessionService;
import com.example.ragcontext.service.ContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final ContextService contextService;

    @PostMapping("/create-chat-session")
    public ResponseEntity<ChatSession> createChatSession(@RequestBody @Valid ChatSessionRequest request) {
        log.info("Creating chat session for context name: {}", request.getContextName());
        
        // If contextId is not provided, create a new context
        String contextId = request.getContextId();
        if (contextId == null || contextId.isEmpty()) {
            Context context = contextService.createContext(
                    request.getContextName(), 
                    request.getContextDescription() != null ? 
                            request.getContextDescription() : 
                            "Created for chat session"
            );
            
            request.setContextId(context.getId());
            log.info("Created new context with ID: {}", context.getId());
        }
        
        return ResponseEntity.ok(chatSessionService.createChatSession(request));
    }
    
    @GetMapping("/contexts/{contextId}/sessions")
    public ResponseEntity<List<ChatSessionDto>> getSessionsByContextId(@PathVariable String contextId) {
        log.info("Getting sessions for context ID: {}", contextId);
        return ResponseEntity.ok(chatSessionService.getSessionsByContextId(contextId));
    }
    
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSessionDto>> getAllSessions() {
        log.info("Getting all chat sessions");
        return ResponseEntity.ok(chatSessionService.getAllSessions());
    }
    
    @GetMapping("/sessions/{sessionId}/history")
    public ResponseEntity<SessionHistoryDto> getSessionHistory(@PathVariable String sessionId) {
        log.info("Getting history for session ID: {}", sessionId);
        return ResponseEntity.ok(chatSessionService.getSessionHistory(sessionId));
    }
    
    @PutMapping("/sessions/{sessionId}/name")
    public ResponseEntity<Void> updateSessionName(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        
        String sessionName = request.get("sessionName");
        if (sessionName == null || sessionName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Updating name for session ID: {} to: {}", sessionId, sessionName);
        chatSessionService.updateSessionName(sessionId, sessionName);
        return ResponseEntity.ok().build();
    }
} 