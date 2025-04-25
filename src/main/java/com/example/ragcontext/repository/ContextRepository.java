package com.example.ragcontext.repository;

import com.example.ragcontext.model.Context;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContextRepository extends MongoRepository<Context, String> {
} 