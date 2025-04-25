package com.example.ragcontext.repository;

import com.example.ragcontext.model.ChatSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionRepository extends MongoRepository<ChatSession, String> {
    List<ChatSession> findByContextIdOrderByLastActivityDesc(String contextId);
    List<ChatSession> findAllByOrderByLastActivityDesc();
} 