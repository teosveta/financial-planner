package com.financialplanner.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaAIService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ai.ollama.model:llama3.2}")
    private String model;

    @Value("${ai.ollama.timeout:30000}")
    private long timeout;

    @Value("${ai.ollama.enabled:true}")
    private boolean enabled;

    /**
     * Generate AI recommendations using Ollama
     */
    public String generateRecommendations(String prompt) {
        if (!enabled) {
            log.warn("AI is disabled. Using fallback recommendations.");
            return "AI service is currently disabled. Please enable it in application.properties.";
        }

        try {
            log.info("Sending prompt to Ollama (model: {})", model);
            
            OllamaRequest request = OllamaRequest.builder()
                    .model(model)
                    .prompt(prompt)
                    .stream(false)
                    .build();

            WebClient webClient = webClientBuilder
                    .baseUrl(ollamaBaseUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            OllamaResponse response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response != null && response.getResponse() != null) {
                log.info("Received AI response: {} characters", response.getResponse().length());
                return response.getResponse();
            }

            log.warn("Empty response from Ollama");
            return "Unable to generate recommendations at this time.";

        } catch (Exception e) {
            log.error("Error calling Ollama AI: {}", e.getMessage(), e);
            return handleAIError(e);
        }
    }

    /**
     * Check if Ollama is available and running
     */
    public boolean isAvailable() {
        if (!enabled) {
            return false;
        }

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(ollamaBaseUrl)
                    .build();

            String response = webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            log.info("Ollama is available");
            return response != null;

        } catch (Exception e) {
            log.warn("Ollama is not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get list of available models
     */
    public List<String> getAvailableModels() {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(ollamaBaseUrl)
                    .build();

            Map<String, Object> response = webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.containsKey("models")) {
                List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");
                return models.stream()
                        .map(m -> (String) m.get("name"))
                        .toList();
            }

        } catch (Exception e) {
            log.error("Error fetching models: {}", e.getMessage());
        }

        return List.of();
    }

    private String handleAIError(Exception e) {
        String message = e.getMessage();
        
        if (message.contains("Connection refused") || message.contains("ConnectException")) {
            return "AI service is not running. Please start Ollama: 'ollama serve'";
        } else if (message.contains("TimeoutException")) {
            return "AI request timed out. The model might be loading or the query is too complex.";
        } else if (message.contains("404")) {
            return "Model '" + model + "' not found. Please pull it: 'ollama pull " + model + "'";
        }
        
        return "Unable to generate AI recommendations. Error: " + message;
    }

    // DTOs for Ollama API
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @lombok.Builder
    public static class OllamaRequest {
        private String model;
        private String prompt;
        private boolean stream;
        private Map<String, Object> options;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OllamaResponse {
        private String model;
        private String response;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        private boolean done;
        
        @JsonProperty("total_duration")
        private Long totalDuration;
    }
}
