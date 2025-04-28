package com.example.ragcontext.service;

import com.example.ragcontext.dto.ChatRequest;
import com.example.ragcontext.dto.ChatResponse;
import com.example.ragcontext.model.Context;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionService chatSessionService;
    private final ContextService contextService;
    private final EmbeddingService embeddingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final OllamaChatModel ollamaChatModel;
    
    private static final int MAX_RELEVANT_MATCHES = 30;

    public void processStreamingChatRequest(ChatRequest request) {
        log.info("Processing chat request for session: {}", request.getSessionId());
        
        // Verify the session exists
        chatSessionService.getSessionById(request.getSessionId());
        
        // If contextId is not provided but contextName is, find the context by name
        // or create a new one if it doesn't exist
        String contextId = request.getContextId();
        String contextName = request.getContextName();
        
        if (contextId == null || contextId.isEmpty()) {
            try {
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
            
            // Create system message with context
            SystemMessage systemMessage = SystemMessage.from(
                    "You are an AI assistant that answers questions based on the provided context.\n\n" +
                    "Context information:\n" + context + "\n\n" +
                    "Answer the question based only on the provided context. If the context doesn't contain the information needed, say 'I don't have enough information to answer this question.'"
            );
            
            // Create user message
            UserMessage userMessage = UserMessage.from(request.getQuestion());
            
            // Get response from Ollama
            AiMessage aiMessage = ollamaChatModel.generate(systemMessage, userMessage).content();
            String responseText = aiMessage.text();
            
            // Save the AI response to MongoDB
            chatSessionService.saveAiMessage(request.getSessionId(), responseText);
            
            // Stream the response
            streamResponse(responseText, request.getSessionId());
            
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
    
    private void streamResponse(String responseText, String sessionId) {
        String destination = "/topic/chat/" + sessionId;
        
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
            log.error("Error streaming response", e);
            throw e;
        }
    }
} 