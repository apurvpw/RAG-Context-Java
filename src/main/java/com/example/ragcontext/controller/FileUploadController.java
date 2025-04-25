package com.example.ragcontext.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Controller
@RequestMapping("/upload")
public class FileUploadController {

    @PostMapping("/file")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        log.info("Received file: {}, size: {}", file.getOriginalFilename(), file.getSize());
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }
        
        try {
            byte[] bytes = file.getBytes();
            log.info("File content length: {}", bytes.length);
            return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Failed to upload file", e);
            return ResponseEntity.status(500).body("Failed to upload: " + e.getMessage());
        }
    }
} 