package com.financialplanner.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

/**
 * Real AI Service using Anthropic Claude API
 * Provides intelligent financial analysis, categorization, and recommendations
 */
@Slf4j
@Service
public class ClaudeAIService {

    @Value("${ai.claude.api-key:}")
    private String apiKey;

    @Value("${ai.claude.model:claude-3-5-sonnet-20241022}")
    private String model;

    @Value("${ai.claude.base-url:https://api.anthropic.com/v1/messages}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public ClaudeAIService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Check if Claude AI service is available
     */
    public boolean isAvailable() {
        boolean available = apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-api-key-here");
        if (available) {
            log.info("Claude AI is configured with API key: {}...{}", 
                apiKey.substring(0, Math.min(15, apiKey.length())), 
                apiKey.substring(Math.max(0, apiKey.length() - 4)));
        } else {
            log.warn("Claude AI is NOT configured - API key is missing or invalid");
        }
        return available;
    }

    /**
     * Categorize transaction using AI
     */
    public String categorizeTransaction(String merchantName, String description, BigDecimal amount) {
        if (!isAvailable()) {
            log.warn("Claude API key not configured, cannot use AI categorization");
            return null;
        }

        String prompt = String.format(
            "Categorize this transaction into ONE of these categories: FOOD, TRAVEL, BILLS, ENTERTAINMENT, SHOPPING, HEALTH, TRANSPORT, OTHER.\n\n" +
            "Merchant: %s\n" +
            "Description: %s\n" +
            "Amount: $%s\n\n" +
            "Respond with ONLY the category name in UPPERCASE, nothing else.",
            merchantName, description, amount.toPlainString()
        );

        try {
            String response = callClaudeAPI(prompt, 50);
            String category = response.trim().toUpperCase();
            
            // Validate category
            Set<String> validCategories = Set.of("FOOD", "TRAVEL", "BILLS", "ENTERTAINMENT", 
                                                  "SHOPPING", "HEALTH", "TRANSPORT", "OTHER");
            if (validCategories.contains(category)) {
                log.info("AI categorized transaction as: {}", category);
                return category;
            }
            
            log.warn("AI returned invalid category: {}", category);
            return null;
            
        } catch (Exception e) {
            log.error("Error calling Claude API for categorization", e);
            return null;
        }
    }

    /**
     * Generate personalized financial recommendations using AI
     */
    public String generateRecommendations(String prompt) {
        if (!isAvailable()) {
            log.warn("Claude API key not configured, using fallback recommendations");
            return "Claude API is not configured. Please add your API key to application.properties";
        }

        try {
            log.info("Calling Claude API for recommendations");
            String response = callClaudeAPI(prompt, 1000);
            log.info("Successfully received AI recommendations");
            return response;
            
        } catch (Exception e) {
            log.error("Error generating AI recommendations: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate AI recommendations", e);
        }
    }

    /**
     * Detect spending anomalies using AI
     */
    public List<String> detectAnomalies(List<Map<String, Object>> recentTransactions,
                                        Map<String, BigDecimal> categoryAverages) {
        if (!isAvailable()) {
            return new ArrayList<>();
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze these recent transactions for unusual patterns or anomalies:\n\n");
        
        for (int i = 0; i < Math.min(10, recentTransactions.size()); i++) {
            Map<String, Object> tx = recentTransactions.get(i);
            prompt.append(String.format("- %s at %s: $%s (%s)\n", 
                tx.get("date"), tx.get("merchant"), tx.get("amount"), tx.get("category")));
        }

        prompt.append("\n\nHistorical averages by category:\n");
        categoryAverages.forEach((cat, avg) -> 
            prompt.append(String.format("- %s: $%s\n", cat, avg.toPlainString())));

        prompt.append("\n\nIdentify any anomalies (unusual large purchases, frequency changes, new merchants). ");
        prompt.append("Return 2-3 brief alerts if found, or 'No anomalies detected' if spending is normal.");
        prompt.append("Format each alert as a separate line starting with 🚨 emoji.");

        try {
            String response = callClaudeAPI(prompt.toString(), 300);
            if (response.toLowerCase().contains("no anomalies")) {
                return new ArrayList<>();
            }
            return parseRecommendations(response);
            
        } catch (Exception e) {
            log.error("Error detecting anomalies", e);
            return new ArrayList<>();
        }
    }

    /**
     * Generate spending insight using AI
     */
    public String generateSpendingInsight(String period, Map<String, BigDecimal> categorySpending, 
                                          BigDecimal totalExpenses) {
        if (!isAvailable()) {
            return "Configure Claude API key for AI-powered insights.";
        }

        String prompt = String.format(
            "Provide a brief, encouraging financial insight for this %s spending summary:\n\n" +
            "Total: $%s\n" +
            "Top categories: %s\n\n" +
            "Give one positive, actionable sentence highlighting a trend or suggestion.",
            period,
            totalExpenses.toPlainString(),
            getTopCategories(categorySpending, 3)
        );

        try {
            return callClaudeAPI(prompt, 150);
        } catch (Exception e) {
            log.error("Error generating insight", e);
            return "Keep tracking your expenses to build better financial habits!";
        }
    }

    /**
     * Get available models (for compatibility with existing code)
     */
    public List<String> getAvailableModels() {
        if (!isAvailable()) {
            return new ArrayList<>();
        }
        return List.of(model);
    }

    /**
     * Core method to call Claude API
     */
    private String callClaudeAPI(String prompt, int maxTokens) {
        if (!isAvailable()) {
            throw new RuntimeException("Claude API key not configured");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("messages", List.of(
            Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("Calling Claude API with model: {}", model);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
                if (content != null && !content.isEmpty()) {
                    String responseText = (String) content.get(0).get("text");
                    log.debug("Claude API response received: {} characters", responseText.length());
                    return responseText;
                }
            }

            throw new RuntimeException("Invalid response from Claude API");

        } catch (Exception e) {
            log.error("Error calling Claude API: {} | Details: {}", e.getMessage(), e.getClass().getName());
            if (e.getMessage() != null) {
                if (e.getMessage().contains("401") || e.getMessage().contains("authentication")) {
                    log.error("Authentication failed - please verify your Claude API key");
                } else if (e.getMessage().contains("429")) {
                    log.error("Rate limit exceeded - too many requests to Claude API");
                } else if (e.getMessage().contains("500") || e.getMessage().contains("503")) {
                    log.error("Claude API server error - service may be temporarily unavailable");
                }
            }
            throw new RuntimeException("Failed to call Claude API: " + e.getMessage(), e);
        }
    }

    /**
     * Parse recommendations from AI response
     */
    private List<String> parseRecommendations(String response) {
        List<String> recommendations = new ArrayList<>();
        String[] lines = response.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            // Remove numbering like "1.", "2.", etc.
            line = line.replaceFirst("^\\d+\\.\\s*", "");
            // Remove bullet points
            line = line.replaceFirst("^[•\\-*]\\s*", "");
            
            if (!line.isEmpty() && line.length() > 20) {
                recommendations.add(line);
            }
        }
        
        return recommendations.isEmpty() ? 
            List.of("Continue monitoring your spending patterns for personalized insights.") : 
            recommendations;
    }

    /**
     * Get top spending categories as a formatted string
     */
    private String getTopCategories(Map<String, BigDecimal> spending, int limit) {
        return spending.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(limit)
            .map(e -> String.format("%s ($%s)", e.getKey(), e.getValue().toPlainString()))
            .reduce((a, b) -> a + ", " + b)
            .orElse("N/A");
    }
}

