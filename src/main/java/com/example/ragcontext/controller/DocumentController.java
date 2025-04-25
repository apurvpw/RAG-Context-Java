package com.example.ragcontext.controller;

import com.example.ragcontext.dto.DocumentDTO;
import com.example.ragcontext.model.Context;
import com.example.ragcontext.service.ContextService;
import com.example.ragcontext.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

    private final ContextService contextService;
    private final DocumentService documentService;

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(
            @RequestParam(value = "contextId", required = false) String contextId,
            @RequestParam("contextName") String contextName,
            @RequestParam(value = "contextDescription", required = false, defaultValue = "") String contextDescription,
            @RequestParam("documentId") String documentId,
            @RequestPart("file") MultipartFile file) {
        
        log.info("Uploading document for context name: {}, document ID: {}", contextName, documentId);
        log.info("File received: {}, size: {} bytes", 
                file.getOriginalFilename(), 
                file.getSize());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        
        try {
            // If contextId is not provided, create a new context
            if (contextId == null || contextId.isEmpty()) {
                Context context = contextService.createContext(contextName, contextDescription);
                contextId = context.getId();
                log.info("Created new context with ID: {}", contextId);
            } else {
                // Verify context exists
                contextService.getContextById(contextId);
            }
            
            // Process document
            DocumentDTO document = documentService.processDocument(contextId, contextName, documentId, file);
            
            return ResponseEntity.ok("Document uploaded and processed successfully. Document ID: " + document.getId());
        } catch (Exception e) {
            log.error("Failed to process document", e);
            return ResponseEntity.status(500).body("Failed to process document: " + e.getMessage());
        }
    }
    
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<DocumentDTO> getDocument(@PathVariable String documentId) {
        DocumentDTO document = documentService.getDocumentById(documentId);
        if (document == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(document);
    }
} 