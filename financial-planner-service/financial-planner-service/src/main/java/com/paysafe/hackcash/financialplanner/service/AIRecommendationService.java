package com.paysafe.hackcash.financialplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paysafe.hackcash.financialplanner.dto.FinancialAnalysisResponse;
import com.paysafe.hackcash.financialplanner.model.SpendingCategory;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AIRecommendationService {
    
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.model}")
    private String model;
    
    public AIRecommendationService(OpenAiService openAiService, ObjectMapper objectMapper) {
        this.openAiService = openAiService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Generate AI-powered financial recommendations with retry logic
     */
    public List<FinancialAnalysisResponse.AIRecommendation> generateRecommendations(
            Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown,
            FinancialAnalysisResponse.AnalysisSummary summary) {
        
        log.info("Generating AI recommendations for user spending");
        
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                String prompt = buildRecommendationPrompt(categoryBreakdown, summary);
                String aiResponse = callOpenAI(prompt);
                return parseRecommendations(aiResponse);
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                log.warn("AI recommendation generation attempt {} failed: {}. Retrying...", 
                        retryCount, e.getMessage());
                
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(1000 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        // Try with simplified AI prompt
        log.error("All retry attempts failed. Trying simplified AI recommendation generation.");
        try {
            return generateRecommendationsWithSimplifiedAI(categoryBreakdown, summary);
        } catch (Exception e) {
            log.error("Simplified AI recommendation generation also failed: {}", e.getMessage());
            throw new RuntimeException("Unable to generate recommendations after multiple attempts. " +
                    "Please ensure OpenAI API is accessible and API key is valid.", lastException);
        }
    }
    
    /**
     * Simplified AI recommendation generation as last resort
     */
    private List<FinancialAnalysisResponse.AIRecommendation> generateRecommendationsWithSimplifiedAI(
            Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown,
            FinancialAnalysisResponse.AnalysisSummary summary) {
        
        log.info("Attempting simplified AI recommendation generation");
        
        // Build a much simpler prompt
        StringBuilder simplePrompt = new StringBuilder("""
            Give me 2-3 short financial tips based on this spending:
            Total Spent: %s %s
            
            Top Categories:
            """.formatted(summary.getTotalSpent(), summary.getCurrency()));
        
        categoryBreakdown.entrySet().stream()
                .filter(e -> e.getKey() != SpendingCategory.TRANSFER && e.getKey() != SpendingCategory.INCOME)
                .sorted((e1, e2) -> e2.getValue().getAmount().compareTo(e1.getValue().getAmount()))
                .limit(3)
                .forEach(entry -> simplePrompt.append(String.format(
                        "- %s: %.1f%%\n",
                        entry.getKey().getDisplayName(),
                        entry.getValue().getPercentage()
                )));
        
        simplePrompt.append("""
            
            Respond with JSON array:
            [{"title":"tip title","description":"tip description","priority":"HIGH"}]
            """);
        
        try {
            String response = callOpenAI(simplePrompt.toString());
            String cleanJson = response.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
            
            JsonNode arrayNode = objectMapper.readTree(cleanJson);
            List<FinancialAnalysisResponse.AIRecommendation> recommendations = new ArrayList<>();
            
            for (JsonNode node : arrayNode) {
                recommendations.add(FinancialAnalysisResponse.AIRecommendation.builder()
                        .type("SAVINGS")
                        .title(node.has("title") ? node.get("title").asText() : "Review Your Spending")
                        .description(node.has("description") ? node.get("description").asText() : 
                                "Review your spending patterns and identify areas for potential savings.")
                        .potentialSavings(BigDecimal.ZERO)
                        .priority(node.has("priority") ? node.get("priority").asText() : "MEDIUM")
                        .build());
            }
            
            return recommendations;
            
        } catch (Exception e) {
            throw new RuntimeException("Simplified AI recommendation generation failed", e);
        }
    }
    
    /**
     * Generate spending insights with natural language using AI
     */
    public List<FinancialAnalysisResponse.SpendingInsight> generateInsights(
            Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown,
            FinancialAnalysisResponse.AnalysisSummary summary) {
        
        log.info("Generating spending insights");
        
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                String prompt = buildInsightsPrompt(categoryBreakdown, summary);
                String aiResponse = callOpenAI(prompt);
                return parseInsights(aiResponse);
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                log.warn("AI insight generation attempt {} failed: {}. Retrying...", 
                        retryCount, e.getMessage());
                
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(1000 * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        // Try with simplified AI prompt
        log.error("All retry attempts failed. Trying simplified AI insight generation.");
        try {
            return generateInsightsWithSimplifiedAI(categoryBreakdown, summary);
        } catch (Exception e) {
            log.error("Simplified AI insight generation also failed: {}", e.getMessage());
            throw new RuntimeException("Unable to generate insights after multiple attempts. " +
                    "Please ensure OpenAI API is accessible and API key is valid.", lastException);
        }
    }
    
    /**
     * Simplified AI insight generation as last resort
     */
    private List<FinancialAnalysisResponse.SpendingInsight> generateInsightsWithSimplifiedAI(
            Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown,
            FinancialAnalysisResponse.AnalysisSummary summary) {
        
        log.info("Attempting simplified AI insight generation");
        
        StringBuilder simplePrompt = new StringBuilder("""
            Give 2-3 brief observations about this spending:
            """);
        
        categoryBreakdown.entrySet().stream()
                .filter(e -> e.getKey() != SpendingCategory.TRANSFER && e.getKey() != SpendingCategory.INCOME)
                .sorted((e1, e2) -> e2.getValue().getAmount().compareTo(e1.getValue().getAmount()))
                .limit(3)
                .forEach(entry -> simplePrompt.append(String.format(
                        "- %s: %s %s (%.1f%%)\n",
                        entry.getKey().getDisplayName(),
                        entry.getValue().getAmount(),
                        summary.getCurrency(),
                        entry.getValue().getPercentage()
                )));
        
        simplePrompt.append("""
            
            Respond with JSON array:
            [{"message":"observation text","sentiment":"POSITIVE|NEUTRAL|NEGATIVE"}]
            """);
        
        try {
            String response = callOpenAI(simplePrompt.toString());
            String cleanJson = response.replaceAll("```json\\n?", "").replaceAll("```\\n?", "").trim();
            
            JsonNode arrayNode = objectMapper.readTree(cleanJson);
            List<FinancialAnalysisResponse.SpendingInsight> insights = new ArrayList<>();
            
            for (JsonNode node : arrayNode) {
                insights.add(FinancialAnalysisResponse.SpendingInsight.builder()
                        .message(node.has("message") ? node.get("message").asText() : 
                                "Your spending patterns show room for optimization.")
                        .category(null)
                        .sentiment(node.has("sentiment") ? node.get("sentiment").asText() : "NEUTRAL")
                        .build());
            }
            
            return insights;
            
        } catch (Exception e) {
            throw new RuntimeException("Simplified AI insight generation failed", e);
        }
    }
    
    private String buildRecommendationPrompt(
            Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown,
            FinancialAnalysisResponse.AnalysisSummary summary) {
        
        StringBuilder prompt = new StringBuilder("""
            You are a professional financial advisor. Analyze this spending data and provide 3-5 actionable recommendations.
            
            Total Spent: %s %s
            Total Income: %s %s
            Net Savings: %s %s
            
            Spending by Category:
            """.formatted(
                summary.getTotalSpent(), summary.getCurrency(),
                summary.getTotalIncome(), summary.getCurrency(),
                summary.getNetSavings(), summary.getCurrency()
        ));
        
        categoryBreakdown.forEach((category, breakdown) -> {
            prompt.append(String.format("""
                - %s: %s %s (%.1f%% of total, %d transactions)
                """,
                category.getDisplayName(),
                breakdown.getAmount(),
                summary.getCurrency(),
                breakdown.getPercentage(),
                breakdown.getTransactionCount()
            ));
        });
        
        prompt.append("""
            
            Provide recommendations as a JSON array with this exact format:
            [
              {
                "type": "SAVINGS|BUDGET|ALERT|OPPORTUNITY",
                "title": "Short actionable title",
                "description": "Detailed explanation and specific action steps",
                "potentialSavings": 0.00,
                "priority": "HIGH|MEDIUM|LOW",
                "relatedCategory": "CATEGORY_NAME"
              }
            ]
            
            Focus on:
            1. Categories with unusually high spending (>30% of total)
            2. Opportunities to reduce expenses
            3. Positive reinforcement for good habits
            4. Specific, actionable advice with numbers
            
            Respond with ONLY the JSON array, no other text.
            """);
        
        return prompt.toString();
    }
    
    private String buildInsightsPrompt(
            Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown,
            FinancialAnalysisResponse.AnalysisSummary summary) {
        
        StringBuilder prompt = new StringBuilder("""
            You are a friendly financial advisor. Provide 3-5 conversational insights about this spending data.
            
            Total Spent: %s %s
            Net Savings: %s %s
            
            Spending by Category:
            """.formatted(
                summary.getTotalSpent(), summary.getCurrency(),
                summary.getNetSavings(), summary.getCurrency()
        ));
        
        categoryBreakdown.forEach((category, breakdown) -> {
            prompt.append(String.format("""
                - %s: %s (%.1f%%)
                """,
                category.getDisplayName(),
                breakdown.getAmount(),
                breakdown.getPercentage()
            ));
        });
        
        prompt.append("""
            
            Provide insights as a JSON array with this exact format:
            [
              {
                "message": "Conversational insight with specific numbers",
                "category": "CATEGORY_NAME",
                "sentiment": "POSITIVE|NEUTRAL|NEGATIVE"
              }
            ]
            
            Make insights:
            1. Conversational and friendly
            2. Include specific percentages and amounts
            3. Mix positive and constructive feedback
            4. Reference typical spending patterns
            
            Respond with ONLY the JSON array, no other text.
            """);
        
        return prompt.toString();
    }
    
    private String callOpenAI(String prompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
            "You are a financial advisor who provides clear, actionable advice. Always respond with valid JSON only."));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
        
        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(1500)
                    .temperature(0.7)
                    .build();
            
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            
            if (result.getChoices() == null || result.getChoices().isEmpty()) {
                throw new RuntimeException("OpenAI returned empty response");
            }
            
            String response = result.getChoices().get(0).getMessage().getContent();
            
            if (response == null || response.trim().isEmpty()) {
                throw new RuntimeException("OpenAI returned null or empty content");
            }
            
            log.debug("OpenAI Response: {}", response);
            return response.trim();
            
        } catch (Exception e) {
            log.error("OpenAI API call failed: {}", e.getMessage());
            throw new RuntimeException("Failed to get response from OpenAI: " + e.getMessage(), e);
        }
    }
    
    private List<FinancialAnalysisResponse.AIRecommendation> parseRecommendations(String aiResponse) {
        try {
            String cleanJson = aiResponse
                .replaceAll("```json\\n?", "")
                .replaceAll("```\\n?", "")
                .trim();
            
            JsonNode arrayNode = objectMapper.readTree(cleanJson);
            List<FinancialAnalysisResponse.AIRecommendation> recommendations = new ArrayList<>();
            
            for (JsonNode node : arrayNode) {
                SpendingCategory relatedCategory = null;
                if (node.has("relatedCategory") && !node.get("relatedCategory").isNull()) {
                    try {
                        relatedCategory = SpendingCategory.valueOf(node.get("relatedCategory").asText());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid category in recommendation: {}", node.get("relatedCategory").asText());
                    }
                }
                
                recommendations.add(FinancialAnalysisResponse.AIRecommendation.builder()
                        .type(node.get("type").asText())
                        .title(node.get("title").asText())
                        .description(node.get("description").asText())
                        .potentialSavings(new BigDecimal(node.get("potentialSavings").asText()))
                        .priority(node.get("priority").asText())
                        .relatedCategory(relatedCategory)
                        .build());
            }
            
            return recommendations;
            
        } catch (Exception e) {
            log.error("Error parsing recommendations: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI recommendations", e);
        }
    }
    
    private List<FinancialAnalysisResponse.SpendingInsight> parseInsights(String aiResponse) {
        try {
            String cleanJson = aiResponse
                .replaceAll("```json\\n?", "")
                .replaceAll("```\\n?", "")
                .trim();
            
            JsonNode arrayNode = objectMapper.readTree(cleanJson);
            List<FinancialAnalysisResponse.SpendingInsight> insights = new ArrayList<>();
            
            for (JsonNode node : arrayNode) {
                SpendingCategory category = null;
                if (node.has("category") && !node.get("category").isNull()) {
                    try {
                        category = SpendingCategory.valueOf(node.get("category").asText());
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid category in insight: {}", node.get("category").asText());
                    }
                }
                
                insights.add(FinancialAnalysisResponse.SpendingInsight.builder()
                        .message(node.get("message").asText())
                        .category(category)
                        .sentiment(node.get("sentiment").asText())
                        .build());
            }
            
            return insights;
            
        } catch (Exception e) {
            log.error("Error parsing insights: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI insights", e);
        }
    }
}
