package com.paysafe.hackcash.financialplanner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paysafe.hackcash.financialplanner.dto.CategorizationResult;
import com.paysafe.hackcash.financialplanner.model.SpendingCategory;
import com.paysafe.hackcash.financialplanner.model.Transaction;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AICategorizationService {
    
    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;
    
    @Value("${openai.api.model}")
    private String model;
    
    @Value("${openai.api.max-tokens}")
    private Integer maxTokens;
    
    @Value("${openai.api.temperature}")
    private Double temperature;
    
    public AICategorizationService(OpenAiService openAiService, ObjectMapper objectMapper) {
        this.openAiService = openAiService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Categorize a single transaction using AI with retry logic
     */
    public CategorizationResult categorizeTransaction(Transaction transaction) {
        log.debug("Categorizing transaction: {}", transaction.getDescription());
        
        int maxRetries = 3;
        int retryCount = 0;
        Exception lastException = null;
        
        while (retryCount < maxRetries) {
            try {
                String prompt = buildCategorizationPrompt(transaction);
                String aiResponse = callOpenAI(prompt);
                return parseCategorizationResponse(aiResponse);
                
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                log.warn("AI categorization attempt {} failed: {}. Retrying...", retryCount, e.getMessage());
                
                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(1000 * retryCount); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        // If all retries failed, try with a simpler AI prompt
        log.error("All retry attempts failed. Trying simplified AI categorization.");
        try {
            return categorizeWithSimplifiedAI(transaction);
        } catch (Exception e) {
            log.error("Simplified AI categorization also failed: {}", e.getMessage());
            throw new RuntimeException("Unable to categorize transaction after multiple attempts. " +
                    "Please ensure OpenAI API is accessible and API key is valid.", lastException);
        }
    }
    
    /**
     * Simplified AI categorization as last resort - still uses AI!
     */
    private CategorizationResult categorizeWithSimplifiedAI(Transaction transaction) {
        log.info("Attempting simplified AI categorization for: {}", transaction.getDescription());
        
        String simplifiedPrompt = String.format("""
            Categorize this transaction in ONE word only: %s
            
            Choose ONLY from: FOOD, TRAVEL, BILLS, ENTERTAINMENT, SHOPPING, HEALTH, TRANSPORT, TRANSFER, INCOME, OTHER
            
            Respond with ONLY the category name, nothing else.
            """,
            transaction.getDescription()
        );
        
        try {
            String response = callOpenAI(simplifiedPrompt).toUpperCase().trim();
            
            // Clean up any extra text
            for (SpendingCategory category : SpendingCategory.values()) {
                if (response.contains(category.name())) {
                    return CategorizationResult.builder()
                            .category(category)
                            .merchantName("Unknown (simplified AI)")
                            .confidenceScore(0.6)
                            .reasoning("Categorized using simplified AI prompt")
                            .build();
                }
            }
            
            // If we still can't parse, default to OTHER
            return CategorizationResult.builder()
                    .category(SpendingCategory.OTHER)
                    .merchantName("Unknown")
                    .confidenceScore(0.3)
                    .reasoning("Could not determine category from AI response")
                    .build();
                    
        } catch (Exception e) {
            throw new RuntimeException("Simplified AI categorization failed", e);
        }
    }
    
    /**
     * Batch categorize multiple transactions for efficiency
     */
    public List<CategorizationResult> categorizeTransactions(List<Transaction> transactions) {
        log.info("Batch categorizing {} transactions", transactions.size());
        
        List<CategorizationResult> results = new ArrayList<>();
        
        // Process in batches to avoid token limits
        int batchSize = 5;
        for (int i = 0; i < transactions.size(); i += batchSize) {
            int end = Math.min(i + batchSize, transactions.size());
            List<Transaction> batch = transactions.subList(i, end);
            
            int maxRetries = 3;
            int retryCount = 0;
            boolean batchSucceeded = false;
            
            while (retryCount < maxRetries && !batchSucceeded) {
                try {
                    String prompt = buildBatchCategorizationPrompt(batch);
                    String aiResponse = callOpenAI(prompt);
                    results.addAll(parseBatchCategorizationResponse(aiResponse, batch));
                    batchSucceeded = true;
                    
                } catch (Exception e) {
                    retryCount++;
                    log.warn("Batch categorization attempt {} failed: {}. Retrying...", retryCount, e.getMessage());
                    
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
            
            // If batch failed after retries, process individually with AI
            if (!batchSucceeded) {
                log.warn("Batch processing failed. Processing transactions individually with AI.");
                for (Transaction tx : batch) {
                    results.add(categorizeTransaction(tx)); // Uses AI with retry logic
                }
            }
        }
        
        return results;
    }
    
    private String buildCategorizationPrompt(Transaction transaction) {
        return String.format("""
            You are a financial transaction categorization expert. Analyze this transaction and categorize it.
            
            Transaction Details:
            - Description: %s
            - Amount: %s %s
            - Type: %s
            - Sender: %s
            - Receiver: %s
            
            Available Categories:
            - FOOD: Food, dining, restaurants, groceries
            - TRAVEL: Travel, flights, hotels, vacation
            - BILLS: Utilities, rent, insurance, subscriptions
            - ENTERTAINMENT: Movies, games, streaming, events
            - SHOPPING: Retail, online shopping, clothing
            - HEALTH: Medical, pharmacy, fitness, wellness
            - TRANSPORT: Uber, taxi, gas, parking, public transport
            - TRANSFER: Money transfers between accounts
            - INCOME: Salary, refunds, cashback
            - OTHER: Anything that doesn't fit above
            
            Respond with ONLY a valid JSON object in this exact format:
            {
              "category": "CATEGORY_NAME",
              "merchantName": "extracted merchant name",
              "confidenceScore": 0.95,
              "reasoning": "brief explanation"
            }
            """,
            transaction.getDescription(),
            transaction.getAmount(),
            transaction.getCurrency(),
            transaction.getType(),
            transaction.getSender(),
            transaction.getReceiver()
        );
    }
    
    private String buildBatchCategorizationPrompt(List<Transaction> transactions) {
        StringBuilder prompt = new StringBuilder("""
            You are a financial transaction categorization expert. Analyze these transactions and categorize each one.
            
            Available Categories:
            - FOOD: Food, dining, restaurants, groceries
            - TRAVEL: Travel, flights, hotels, vacation
            - BILLS: Utilities, rent, insurance, subscriptions
            - ENTERTAINMENT: Movies, games, streaming, events
            - SHOPPING: Retail, online shopping, clothing
            - HEALTH: Medical, pharmacy, fitness, wellness
            - TRANSPORT: Uber, taxi, gas, parking, public transport
            - TRANSFER: Money transfers between accounts
            - INCOME: Salary, refunds, cashback
            - OTHER: Anything that doesn't fit above
            
            Transactions:
            """);
        
        for (int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);
            prompt.append(String.format("""
                
                %d. Description: %s
                   Amount: %s %s
                   Type: %s
                """, 
                i + 1,
                tx.getDescription(),
                tx.getAmount(),
                tx.getCurrency(),
                tx.getType()
            ));
        }
        
        prompt.append("""
            
            Respond with ONLY a valid JSON array in this exact format:
            [
              {
                "index": 1,
                "category": "CATEGORY_NAME",
                "merchantName": "extracted merchant name",
                "confidenceScore": 0.95,
                "reasoning": "brief explanation"
              }
            ]
            """);
        
        return prompt.toString();
    }
    
    private String callOpenAI(String prompt) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), 
            "You are a precise financial analyst. Always respond with valid JSON only. No markdown, no code blocks, just raw JSON."));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), prompt));
        
        try {
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(temperature)
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
    
    private CategorizationResult parseCategorizationResponse(String aiResponse) {
        try {
            // Clean up potential markdown code blocks
            String cleanJson = aiResponse
                .replaceAll("```json\\n?", "")
                .replaceAll("```\\n?", "")
                .trim();
            
            JsonNode node = objectMapper.readTree(cleanJson);
            
            return CategorizationResult.builder()
                    .category(SpendingCategory.valueOf(node.get("category").asText()))
                    .merchantName(node.get("merchantName").asText())
                    .confidenceScore(node.get("confidenceScore").asDouble())
                    .reasoning(node.get("reasoning").asText())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI categorization response", e);
        }
    }
    
    private List<CategorizationResult> parseBatchCategorizationResponse(String aiResponse, List<Transaction> transactions) {
        try {
            String cleanJson = aiResponse
                .replaceAll("```json\\n?", "")
                .replaceAll("```\\n?", "")
                .trim();
            
            JsonNode arrayNode = objectMapper.readTree(cleanJson);
            List<CategorizationResult> results = new ArrayList<>();
            
            for (JsonNode node : arrayNode) {
                results.add(CategorizationResult.builder()
                        .category(SpendingCategory.valueOf(node.get("category").asText()))
                        .merchantName(node.get("merchantName").asText())
                        .confidenceScore(node.get("confidenceScore").asDouble())
                        .reasoning(node.get("reasoning").asText())
                        .build());
            }
            
            return results;
            
        } catch (Exception e) {
            log.error("Error parsing batch AI response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse batch AI categorization response", e);
        }
    }
}
