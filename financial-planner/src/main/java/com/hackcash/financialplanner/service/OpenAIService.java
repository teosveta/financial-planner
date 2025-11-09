package com.hackcash.financialplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Service - Real AI Integration for Transaction Categorization & Recommendations
 * Uses GPT-4 for intelligent analysis
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAIService {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.api.url}")
    private String apiUrl;
    
    @Value("${openai.model}")
    private String model;
    
    @Value("${openai.timeout}")
    private long timeout;
    
    /**
     * Categorize a transaction using OpenAI
     */
    public String categorizeTransaction(String merchantName, String description, Double amount) {
        String prompt = buildCategorizationPrompt(merchantName, description, amount);
        
        try {
            String response = callOpenAI(prompt, 100);
            log.info("OpenAI categorization response: {}", response);
            return response.trim().toUpperCase();
        } catch (Exception e) {
            log.error("Failed to categorize with OpenAI: {}", e.getMessage());
            return null; // Will trigger fallback logic
        }
    }
    
    /**
     * Generate AI-powered savings recommendations
     */
    public String generateRecommendations(Map<String, Object> analysisData) {
        String prompt = buildRecommendationPrompt(analysisData);
        
        try {
            String response = callOpenAI(prompt, 500);
            log.info("OpenAI recommendations generated successfully");
            return response;
        } catch (Exception e) {
            log.error("Failed to generate recommendations with OpenAI: {}", e.getMessage());
            return null; // Will trigger fallback logic
        }
    }
    
    /**
     * Call OpenAI API with retry logic
     */
    private String callOpenAI(String prompt, int maxTokens) {
        WebClient client = webClientBuilder
            .baseUrl(apiUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        
        Map<String, Object> requestBody = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", "You are a financial advisor AI. Provide concise, actionable advice."),
                Map.of("role", "user", "content", prompt)
            ),
            "max_tokens", maxTokens,
            "temperature", 0.7
        );
        
        try {
            String response = client.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeout))
                .block();
            
            return extractContentFromResponse(response);
        } catch (WebClientResponseException e) {
            log.error("OpenAI API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling OpenAI: {}", e.getMessage());
            throw new RuntimeException("Failed to call OpenAI: " + e.getMessage());
        }
    }
    
    /**
     * Extract content from OpenAI response JSON
     */
    private String extractContentFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Failed to parse OpenAI response: {}", e.getMessage());
            throw new RuntimeException("Invalid OpenAI response format");
        }
    }
    
    /**
     * Build prompt for transaction categorization
     */
    private String buildCategorizationPrompt(String merchantName, String description, Double amount) {
        return String.format("""
            Categorize this transaction into ONE of these categories (respond with ONLY the category name):
            FOOD, TRAVEL, BILLS, ENTERTAINMENT, SHOPPING, HEALTH, TRANSPORT, EDUCATION, GROCERIES, INCOME, SAVINGS, OTHER
            
            Transaction details:
            - Merchant: %s
            - Description: %s
            - Amount: $%.2f
            
            Respond with ONLY the category name (e.g., "FOOD" or "TRANSPORT").
            """, merchantName, description, amount);
    }
    
    /**
     * Build prompt for savings recommendations
     */
    private String buildRecommendationPrompt(Map<String, Object> analysisData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this spending data and provide 3-5 concise savings recommendations:\n\n");
        
        // Add spending by category
        if (analysisData.containsKey("categoryBreakdown")) {
            prompt.append("Category Breakdown:\n");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> categories = (List<Map<String, Object>>) analysisData.get("categoryBreakdown");
            for (Map<String, Object> cat : categories) {
                prompt.append(String.format("- %s: $%.2f (%.1f%%)\n", 
                    cat.get("category"), 
                    cat.get("totalAmount"), 
                    cat.get("percentage")));
            }
        }
        
        // Add totals
        if (analysisData.containsKey("totalExpenses")) {
            prompt.append(String.format("\nTotal Expenses: $%.2f\n", analysisData.get("totalExpenses")));
        }
        
        // Add comparison data if available
        if (analysisData.containsKey("previousPeriodExpenses")) {
            prompt.append(String.format("Previous Period: $%.2f\n", analysisData.get("previousPeriodExpenses")));
        }
        
        prompt.append("\nProvide specific, actionable recommendations. Format each as a single sentence. ");
        prompt.append("Focus on categories that are above 30% or showing significant increases.");
        
        return prompt.toString();
    }
}
