package com.example.ragcontext.service;

import com.example.ragcontext.dto.ChatRequest;
import com.example.ragcontext.dto.ChatResponse;
import com.example.ragcontext.model.ChatSession;
import com.example.ragcontext.model.Context;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionService chatSessionService;
    private final ContextService contextService;
    private final EmbeddingService embeddingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate;
    private final com.example.ragcontext.config.AppProperties appProperties;
    
    private static final int MAX_RELEVANT_MATCHES = 30;

    public void processStreamingChatRequest(ChatRequest request) {
        log.info("Processing chat request for session: {}", request.getSessionId());
        
        // Verify the session exists
        ChatSession session = chatSessionService.getSessionById(request.getSessionId());
        
        // If contextId is not provided but contextName is, find the context by name
        // or create a new one if it doesn't exist
        String contextId = request.getContextId();
        String contextName = request.getContextName();
        
        if (contextId == null || contextId.isEmpty()) {
            try {
                // For simplicity, we're creating a new context with an empty description
                // In a real application, you might want to search for existing contexts by name first
                Context context = contextService.createContext(contextName, "Created during chat");
                contextId = context.getId();
                log.info("Created new context with ID: {} for chat session", contextId);
            } catch (Exception e) {
                log.error("Error creating context for chat", e);
                sendErrorResponse(request.getSessionId(), "Failed to create context: " + e.getMessage());
                return;
            }
        }
        
        // Save user message to MongoDB
        chatSessionService.saveUserMessage(request.getSessionId(), request.getQuestion());
        
        // Create collection name from context id and name
        String collectionName = contextId + "_" + contextName;
        
        try {
            // Retrieve relevant context from ChromaDB
            List<EmbeddingMatch<TextSegment>> relevantMatches = embeddingService.findRelevantEmbeddings(
                    collectionName, 
                    request.getQuestion(), 
                    MAX_RELEVANT_MATCHES);
            
            // Build a context string from relevant matches
            String context = buildContext(relevantMatches);
            
            // Create prompt with context
            String prompt = String.format(
                    "You are an AI assistant that answers questions based on the provided context.\n\n" +
                    "Context information:\n%s\n\n" +
                    "User question: %s\n\n" +
                    "Answer the question based only on the provided context. If the context doesn't contain the information needed, say 'I don't have enough information to answer this question.'",
                    context, request.getQuestion());
            
            // Stream the response to the user
            streamChatResponse(prompt, request.getSessionId());
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            sendErrorResponse(request.getSessionId(), "Error processing your request: " + e.getMessage());
        }
    }
    
    private void sendErrorResponse(String sessionId, String errorMessage) {
        String destination = "/topic/chat/" + sessionId;
        ChatResponse errorResponse = ChatResponse.builder()
                .sessionId(sessionId)
                .message(errorMessage)
                .done(true)
                .build();
        
        messagingTemplate.convertAndSend(destination, errorResponse);
        
        // Save error message as AI response
        chatSessionService.saveAiMessage(sessionId, "Error: " + errorMessage);
    }
    
    private String buildContext(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));
    }
    
    private void streamChatResponse(String prompt, String sessionId) {
        // Create destination for this session
        String destination = "/topic/chat/" + sessionId;
        
        try {
            // First get the complete response
            String responseText = getResponseFromOllama(prompt);
            
            // Save the full AI response to MongoDB
            chatSessionService.saveAiMessage(sessionId, responseText);
            
            // Then simulate streaming by breaking it into words
            simulateStreaming(responseText, sessionId, destination);
        } catch (Exception e) {
            log.error("Error in chat streaming", e);
            ChatResponse errorResponse = ChatResponse.builder()
                    .sessionId(sessionId)
                    .message("Error processing your request: " + e.getMessage())
                    .done(true)
                    .build();
            
            messagingTemplate.convertAndSend(destination, errorResponse);
            
            // Save error message as AI response
            chatSessionService.saveAiMessage(sessionId, "Error: " + e.getMessage());
        }
    }
    
    private String getResponseFromOllama(String prompt) {
        try {
            String apiUrl = appProperties.getOllama().getUrl() + "/api/generate";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", appProperties.getOllama().getChatModel());
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            
            // Add options for better response quality
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0.7);
            requestBody.put("options", options);
            
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
            
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return (String) responseEntity.getBody().get("response");
            } else {
                throw new RuntimeException("Failed to get response from Ollama API: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error getting response from Ollama", e);
            throw new RuntimeException("Error communicating with Ollama API", e);
        }
    }
    
    private void simulateStreaming(String responseText, String sessionId, String destination) {
        try {
            // Split the response into words and stream them one by one
            String[] words = responseText.split("\\s+");
            for (String word : words) {
                ChatResponse chatResponse = ChatResponse.builder()
                        .sessionId(sessionId)
                        .message(word + " ")
                        .done(false)
                        .build();
                
                messagingTemplate.convertAndSend(destination, chatResponse);
                
                // Add a small delay to simulate streaming
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // Send completion message
            ChatResponse completionResponse = ChatResponse.builder()
                    .sessionId(sessionId)
                    .message("")
                    .done(true)
                    .build();
            
            messagingTemplate.convertAndSend(destination, completionResponse);
            log.info("Completed streaming response for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error simulating streaming", e);
            throw e;
        }
    }
} 