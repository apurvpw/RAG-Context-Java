package com.example.ragcontext.service;

import com.example.ragcontext.dto.ContextRequest;
import com.example.ragcontext.model.Context;
import com.example.ragcontext.repository.ContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContextService {

    private final ContextRepository contextRepository;

    public Context createContext(ContextRequest request) {
        log.info("Creating new context with name: {}", request.getName());
        
        Context context = Context.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        
        return contextRepository.save(context);
    }
    
    public Context createContext(String name, String description) {
        log.info("Creating new context with name: {}", name);
        
        Context context = Context.builder()
                .name(name)
                .description(description)
                .build();
        
        return contextRepository.save(context);
    }

    public Context getContextById(String id) {
        return contextRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Context not found with id: " + id));
    }
    
    public List<Context> getAllContexts() {
        log.info("Getting all contexts");
        return contextRepository.findAll();
    }
} 