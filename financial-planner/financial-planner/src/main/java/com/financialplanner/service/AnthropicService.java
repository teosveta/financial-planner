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

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnthropicService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${ai.anthropic.api-key:}")
    private String apiKey;

    @Value("${ai.anthropic.model:claude-3-5-sonnet-20241022}")
    private String model;

    @Value("${ai.anthropic.base-url:https://api.anthropic.com/v1/messages}")
    private String baseUrl;

    /**
     * Check if Anthropic is available and configured
     */
    public boolean isAvailable() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("Anthropic API key is not configured");
            return false;
        }
        return true;
    }

    /**
     * Categorize a transaction using Claude
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

            String response = callAnthropic(prompt);
            if (response != null) {
                String category = response.trim().toUpperCase();
                log.info("Anthropic categorized '{}' as {}", merchantName, category);
                return category;
            }
        } catch (Exception e) {
            log.error("Error calling Anthropic for categorization: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Generate AI recommendations
     */
    public String generateRecommendations(String prompt) {
        if (!isAvailable()) {
            log.warn("Anthropic is not available. API key not configured.");
            return "AI service is currently not configured. Please add your Anthropic API key.";
        }

        try {
            log.info("Sending prompt to Anthropic Claude (model: {})", model);
            return callAnthropic(prompt);
        } catch (Exception e) {
            log.error("Error calling Anthropic: {}", e.getMessage(), e);
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

            return callAnthropic(prompt);
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

            return callAnthropic(prompt.toString());
        } catch (Exception e) {
            log.error("Error generating spending insight: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get available models
     */
    public List<String> getAvailableModels() {
        return List.of(model, "claude-3-5-sonnet-20241022", "claude-3-opus-20240229", "claude-3-haiku-20240307");
    }

    /**
     * Call Anthropic API
     */
    private String callAnthropic(String prompt) {
        try {
            AnthropicRequest request = new AnthropicRequest();
            request.setModel(model);
            request.setMessages(List.of(
                new AnthropicMessage("user", prompt)
            ));
            request.setMaxTokens(1024);
            request.setTemperature(0.7);

            WebClient webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

            AnthropicResponse response = webClient.post()
                .uri(baseUrl)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AnthropicResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            if (response != null && response.getContent() != null && !response.getContent().isEmpty()) {
                String content = response.getContent().get(0).getText();
                log.info("Received Anthropic response: {} characters", content.length());
                return content;
            }

            log.warn("Empty response from Anthropic");
            return null;

        } catch (Exception e) {
            log.error("Error calling Anthropic API: {}", e.getMessage(), e);
            throw e;
        }
    }

    // DTOs for Anthropic API
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnthropicRequest {
        private String model;
        private List<AnthropicMessage> messages;
        
        @JsonProperty("max_tokens")
        private Integer maxTokens;
        
        private Double temperature;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnthropicMessage {
        private String role;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnthropicResponse {
        private String id;
        private String type;
        private String role;
        private List<ContentBlock> content;
        private String model;
        
        @JsonProperty("stop_reason")
        private String stopReason;
        
        @JsonProperty("stop_sequence")
        private String stopSequence;
        
        private Usage usage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentBlock {
        private String type;
        private String text;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {
        @JsonProperty("input_tokens")
        private Integer inputTokens;
        
        @JsonProperty("output_tokens")
        private Integer outputTokens;
    }
}
