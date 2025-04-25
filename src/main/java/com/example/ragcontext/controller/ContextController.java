package com.example.ragcontext.controller;

import com.example.ragcontext.dto.EmbeddingRequest;
import com.example.ragcontext.model.Context;
import com.example.ragcontext.service.ContextService;
import com.example.ragcontext.service.EmbeddingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContextController {

    private final ContextService contextService;
    private final EmbeddingService embeddingService;

    @GetMapping("/contexts")
    public ResponseEntity<List<Context>> getAllContexts() {
        log.info("Getting all contexts");
        return ResponseEntity.ok(contextService.getAllContexts());
    }
    
    @PostMapping(value = "/create-context-embeddings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Context> createContextAndEmbeddings(
            @RequestParam("contextName") String contextName,
            @RequestParam("contextDescription") String contextDescription,
            @RequestPart(value = "document", required = false) MultipartFile document,
            @RequestParam(value = "documentUrl", required = false) String documentUrl) {
        
        log.info("Creating context with name: {} and processing document", contextName);
        
        // Create embedding request
        EmbeddingRequest request = new EmbeddingRequest();
        request.setContextName(contextName);
        request.setContextDescription(contextDescription);
        request.setDocument(document);
        request.setDocumentUrl(documentUrl);
        
        // First create the context
        Context context = contextService.createContext(contextName, contextDescription);
        request.setContextId(context.getId());
        
        // Then create embeddings
        embeddingService.createEmbeddings(request);
        
        return ResponseEntity.ok(context);
    }
    
    // Simple test endpoint for file upload
    @PostMapping(value = "/test-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> testFileUpload(
            @RequestPart("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        
        log.info("Received file: {}, size: {} bytes", 
                file.getOriginalFilename(), 
                file.getSize());
                
        return ResponseEntity.ok("File received successfully: " + file.getOriginalFilename());
    }
} 