package com.financialplanner.service;

import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAIService {

    private final WebClient.Builder webClientBuilder;
    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${ai.openai.base-url:https://api.openai.com/v1/chat/completions}")
    private String baseUrl;

    /**
     * Check if OpenAI is available and configured
     */
    public boolean isAvailable() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("OpenAI API key is not configured");
            return false;
        }
        return true;
    }

    /**
     * Categorize a transaction using OpenAI
     */
    public String categorizeTransaction(String merchantName, String description, BigDecimal amount) {
        if (!isAvailable()) {
            return null;
        }

        try {
            String prompt = String.format(
                "Categorize this transaction into ONE of these exact categories: FOOD, TRAVEL, BILLS, ENTERTAINMENT, SHOPPING, HEALTH, TRANSPORT, OTHER\n" +
                "Merchant: %s\n" +
                "Description: %s\n" +
                "Amount: $%.2f\n\n" +
                "Reply with ONLY the category name, nothing else.",
                merchantName, description, amount
            );

            String response = callOpenAI(prompt);
            if (response != null) {
                String category = response.trim().toUpperCase();
                log.info("OpenAI categorized '{}' as {}", merchantName, category);
                return category;
            }
        } catch (Exception e) {
            log.error("Error calling OpenAI for categorization: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Generate AI recommendations
     */
    public String generateRecommendations(String prompt) {
        if (!isAvailable()) {
            log.warn("OpenAI is not available. API key not configured.");
            return "AI service is currently not configured. Please add your OpenAI API key.";
        }

        try {
            log.info("Sending prompt to OpenAI (model: {})", model);
            return callOpenAI(prompt);
        } catch (Exception e) {
            log.error("Error calling OpenAI: {}", e.getMessage(), e);
            return "Unable to generate AI recommendations at this time.";
        }
    }

    /**
     * Generate spending insights - overloaded for transaction-level
     */
    public String generateSpendingInsight(String merchantName, BigDecimal amount, String category) {
        if (!isAvailable()) {
            return null;
        }

        try {
            String prompt = String.format(
                "Generate a brief spending insight for this transaction:\n" +
                "Merchant: %s\n" +
                "Amount: $%.2f\n" +
                "Category: %s\n\n" +
                "Provide a one-sentence insight about this expense.",
                merchantName, amount, category
            );

            return callOpenAI(prompt);
        } catch (Exception e) {
            log.error("Error generating spending insight: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Generate spending insights - overloaded for period-level analysis
     */
    public String generateSpendingInsight(String period, Map<String, BigDecimal> categorySpending, BigDecimal totalExpenses) {
        if (!isAvailable()) {
            return null;
        }

        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate spending insights for this ").append(period).append(" period:\n");
            prompt.append("Total Expenses: $").append(totalExpenses).append("\n\n");
            prompt.append("Category Breakdown:\n");
            categorySpending.forEach((category, amount) -> 
                prompt.append("- ").append(category).append(": $").append(amount).append("\n")
            );
            prompt.append("\nProvide 2-3 actionable insights about this spending pattern.");

            return callOpenAI(prompt.toString());
        } catch (Exception e) {
            log.error("Error generating spending insight: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get available models (OpenAI doesn't provide this easily, so return configured model)
     */
    public List<String> getAvailableModels() {
        return List.of(model);
    }

    /**
     * Call OpenAI API
     */
    private String callOpenAI(String prompt) {
        try {
            OpenAIRequest request = new OpenAIRequest();
            request.setModel(model);
            request.setMessages(List.of(
                Map.of("role", "user", "content", prompt)
            ));
            request.setTemperature(0.7);
            request.setMaxTokens(500);

            WebClient webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

            OpenAIResponse response = webClient.post()
                .uri(baseUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAIResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                log.info("Received OpenAI response: {} characters", content.length());
                return content;
            }

            log.warn("Empty response from OpenAI");
            return null;

        } catch (Exception e) {
            log.error("Error calling OpenAI API: {}", e.getMessage(), e);
            throw e;
        }
    }

    // DTOs for OpenAI API
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAIRequest {
        private String model;
        private List<Map<String, String>> messages;
        private Double temperature;
        
        @JsonProperty("max_tokens")
        private Integer maxTokens;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAIResponse {
        private String id;
        private String object;
        private Long created;
        private String model;
        private List<Choice> choices;
        private Usage usage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {
        private Integer index;
        private Message message;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}
