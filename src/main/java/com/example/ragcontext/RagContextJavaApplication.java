package com.example.ragcontext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class RagContextJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagContextJavaApplication.class, args);
    }
} 