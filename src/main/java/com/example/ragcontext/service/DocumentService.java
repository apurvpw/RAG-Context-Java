package com.example.ragcontext.service;

import com.example.ragcontext.dto.DocumentDTO;
import com.example.ragcontext.dto.EmbeddingRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final EmbeddingService embeddingService;
    
    // In-memory storage for document metadata (would be replaced with database in production)
    private final Map<String, DocumentDTO> documentMap = new HashMap<>();

    public DocumentDTO processDocument(String contextId, String contextName, String documentId, MultipartFile file) {
        log.info("Processing document for context: {}, document ID: {}", contextId, documentId);
        
        // Create embedding request
        EmbeddingRequest request = new EmbeddingRequest();
        request.setContextId(contextId);
        request.setContextName(contextName);
        request.setDocument(file);
        
        // Process the file and create embeddings
        embeddingService.createEmbeddings(request);
        
        // Create and store document metadata
        DocumentDTO document = DocumentDTO.builder()
                .id(documentId != null ? documentId : UUID.randomUUID().toString())
                .contextId(contextId)
                .filename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .build();
        
        // Store document metadata
        documentMap.put(document.getId(), document);
        
        log.info("Document processed successfully: {}", document.getFilename());
        return document;
    }
    
    public DocumentDTO getDocumentById(String documentId) {
        return documentMap.get(documentId);
    }
} 