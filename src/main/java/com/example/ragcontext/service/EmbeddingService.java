package com.example.ragcontext.service;

import com.example.ragcontext.config.AppProperties;
import com.example.ragcontext.dto.EmbeddingRequest;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final ContextService contextService;
    private final EmbeddingModel embeddingModel;
    private final AppProperties appProperties;

    @SneakyThrows
    public void createEmbeddings(EmbeddingRequest request) {
        log.info("Creating embeddings for context: {}", request.getContextId());
        
        // Verify the context exists
        contextService.getContextById(request.getContextId());
        
        String text;
        
        if (request.getDocument() != null) {
            // Handle file upload
            text = extractTextFromFile(request.getDocument());
        } else if (request.getDocumentUrl() != null) {
            // Handle URL
            text = extractTextFromUrl(request.getDocumentUrl());
        } else {
            throw new IllegalArgumentException("Either document file or URL must be provided");
        }
        
        // Create document from text
        Document document = Document.from(text);
        
        // Create collection name from context id and name
        String collectionName = request.getContextId() + "_" + request.getContextName();
        
        // Store embeddings in ChromaDB
        storeEmbeddings(document, collectionName);
        
        log.info("Embeddings created successfully for context: {}", request.getContextId());
    }
    
    private String extractTextFromFile(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        
        if (filename == null) {
            return new String(file.getBytes());
        }
        
        if (filename.toLowerCase().endsWith(".pdf")) {
            return extractTextFromPdf(file.getBytes());
        } else if (filename.toLowerCase().endsWith(".doc") || filename.toLowerCase().endsWith(".docx")) {
            return extractTextFromWord(file.getBytes());
        } else {
            // Default to text
            return new String(file.getBytes());
        }
    }
    
    private String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            PDFTextStripper textStripper = new PDFTextStripper();
            return textStripper.getText(document);
        }
    }
    
    private String extractTextFromWord(byte[] wordBytes) throws IOException {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(wordBytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
    
    private String extractTextFromUrl(String url) throws IOException {
        if (url.toLowerCase().endsWith(".pdf")) {
            // Handle PDF URL
            try (InputStream inputStream = new URL(url).openStream()) {
                return extractTextFromPdf(inputStream.readAllBytes());
            }
        } else if (url.toLowerCase().endsWith(".doc") || url.toLowerCase().endsWith(".docx")) {
            // Handle Word document URL
            try (InputStream inputStream = new URL(url).openStream()) {
                return extractTextFromWord(inputStream.readAllBytes());
            }
        } else {
            // Handle web page using JSoup directly
            org.jsoup.nodes.Document jsoupDoc = Jsoup.connect(url).get();
            return jsoupDoc.text();
        }
    }
    
    private void storeEmbeddings(Document document, String collectionName) {
        // Split document into chunks
        DocumentSplitter splitter = DocumentSplitters.recursive(
                appProperties.getDocument().getChunkSize(),
                appProperties.getDocument().getChunkOverlap());
        
        // Split document into segments
        List<TextSegment> segments = splitter.split(document);
        
        // Create a collection-specific store
        ChromaEmbeddingStore collectionStore = ChromaEmbeddingStore.builder()
                .baseUrl(appProperties.getChroma().getUrl())
                .collectionName(collectionName)
                .build();
        
        // Embed each segment and add to store
        for (TextSegment segment : segments) {
            var embedding = embeddingModel.embed(segment.text()).content();
            collectionStore.add(embedding, segment);
        }
    }
    
    public List<EmbeddingMatch<TextSegment>> findRelevantEmbeddings(String collectionName, String query, int maxResults) {
        // Create a collection-specific store
        ChromaEmbeddingStore collectionStore = ChromaEmbeddingStore.builder()
                .baseUrl(appProperties.getChroma().getUrl())
                .collectionName(collectionName)
                .build();
        
        // Embed query and find relevant matches
        return collectionStore.findRelevant(
                embeddingModel.embed(query).content(),
                maxResults);
    }
} 