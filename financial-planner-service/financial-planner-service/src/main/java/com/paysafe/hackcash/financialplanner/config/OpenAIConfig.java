package com.paysafe.hackcash.financialplanner.config;

import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class OpenAIConfig {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.timeout-seconds:90}")
    private int timeoutSeconds;
    
    @Bean
    public OpenAiService openAiService() {
        log.info("Initializing OpenAI service with {}s timeout", timeoutSeconds);
        
        if (apiKey == null || apiKey.isEmpty() || apiKey.startsWith("${")) {
            log.error("OpenAI API key is not configured properly!");
            throw new IllegalStateException(
                "OpenAI API key is required. Please set OPENAI_API_KEY environment variable " +
                "or configure it in application.yml"
            );
        }
        
        try {
            return new OpenAiService(apiKey, Duration.ofSeconds(timeoutSeconds));
        } catch (Exception e) {
            log.error("Failed to initialize OpenAI service: {}", e.getMessage());
            throw new RuntimeException("Could not initialize OpenAI service. Check your API key and network connectivity.", e);
        }
    }
}
