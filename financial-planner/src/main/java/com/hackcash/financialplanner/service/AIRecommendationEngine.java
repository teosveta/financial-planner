package com.hackcash.financialplanner.service;

import com.hackcash.financialplanner.dto.CategoryAnalysisDTO;
import com.hackcash.financialplanner.dto.RecommendationDTO;
import com.hackcash.financialplanner.dto.SpendingAnalysisDTO;
import com.hackcash.financialplanner.model.TransactionCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Recommendation Engine
 * Primary: OpenAI-powered recommendations
 * Fallback: Rule-based intelligent recommendations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIRecommendationEngine {
    
    private final OpenAIService openAIService;
    
    /**
     * Generate personalized savings recommendations
     */
    @Cacheable(value = "ai-recommendations", key = "#analysis.periodStart + '-' + #analysis.periodEnd")
    public List<RecommendationDTO> generateRecommendations(SpendingAnalysisDTO analysis) {
        log.info("Generating AI recommendations for period {} to {}", 
            analysis.getPeriodStart(), analysis.getPeriodEnd());
        
        // Try OpenAI first
        try {
            String aiRecommendations = openAIService.generateRecommendations(
                buildAnalysisData(analysis)
            );
            
            if (aiRecommendations != null && !aiRecommendations.isEmpty()) {
                List<RecommendationDTO> recommendations = parseAIRecommendations(aiRecommendations);
                if (!recommendations.isEmpty()) {
                    log.info("Generated {} AI recommendations", recommendations.size());
                    return recommendations;
                }
            }
        } catch (Exception e) {
            log.warn("AI recommendation generation failed, using fallback: {}", e.getMessage());
        }
        
        // Fallback to rule-based recommendations
        return generateFallbackRecommendations(analysis);
    }
    
    /**
     * Build analysis data map for AI prompt
     */
    private Map<String, Object> buildAnalysisData(SpendingAnalysisDTO analysis) {
        Map<String, Object> data = new HashMap<>();
        data.put("totalExpenses", analysis.getTotalExpenses());
        data.put("totalIncome", analysis.getTotalIncome());
        data.put("savingsRate", analysis.getSavingsRate());
        data.put("trendPercentage", analysis.getTrendPercentage());
        
        if (analysis.getPreviousPeriodExpenses() != null) {
            data.put("previousPeriodExpenses", analysis.getPreviousPeriodExpenses());
        }
        
        // Category breakdown
        List<Map<String, Object>> categoryData = analysis.getCategoryBreakdown().stream()
            .map(cat -> Map.of(
                "category", cat.getCategoryDisplayName(),
                "totalAmount", cat.getTotalAmount(),
                "percentage", cat.getPercentage(),
                "recommended", cat.getRecommendedPercentage()
            ))
            .collect(Collectors.toList());
        
        data.put("categoryBreakdown", categoryData);
        
        return data;
    }
    
    /**
     * Parse AI recommendations into structured DTOs
     */
    private List<RecommendationDTO> parseAIRecommendations(String aiText) {
        List<RecommendationDTO> recommendations = new ArrayList<>();
        
        // Split by line or numbered points
        String[] lines = aiText.split("\n");
        int priority = 1;
        
        for (String line : lines) {
            String cleaned = line.trim()
                .replaceAll("^[0-9]+\\.\\s*", "") // Remove "1. ", "2. " etc
                .replaceAll("^-\\s*", "") // Remove "- "
                .replaceAll("^\\*\\s*", ""); // Remove "* "
            
            if (!cleaned.isEmpty() && cleaned.length() > 20) { // Filter out noise
                RecommendationDTO rec = RecommendationDTO.builder()
                    .message(cleaned)
                    .priority(priority++)
                    .source("AI")
                    .actionable(true)
                    .build();
                
                recommendations.add(rec);
            }
        }
        
        return recommendations;
    }
    
    /**
     * Generate rule-based fallback recommendations
     */
    private List<RecommendationDTO> generateFallbackRecommendations(SpendingAnalysisDTO analysis) {
        List<RecommendationDTO> recommendations = new ArrayList<>();
        int priority = 1;
        
        // Check overall spending trend
        if (analysis.getTrendPercentage() > 10) {
            recommendations.add(RecommendationDTO.builder()
                .message(String.format("Your expenses increased by %.1f%% compared to last period. " +
                    "Consider reviewing your recent spending habits.", analysis.getTrendPercentage()))
                .priority(priority++)
                .source("Rule-Based")
                .actionable(true)
                .build());
        }
        
        // Check savings rate
        if (analysis.getSavingsRate() < 10 && analysis.getTotalIncome().compareTo(BigDecimal.ZERO) > 0) {
            recommendations.add(RecommendationDTO.builder()
                .message(String.format("Your savings rate is %.1f%%. Financial experts recommend saving at least 20%% of your income. " +
                    "Try the 50/30/20 rule: 50%% needs, 30%% wants, 20%% savings.", analysis.getSavingsRate()))
                .priority(priority++)
                .source("Rule-Based")
                .actionable(true)
                .build());
        }
        
        // Analyze each category
        for (CategoryAnalysisDTO category : analysis.getCategoryBreakdown()) {
            double excessPercentage = category.getPercentage() - category.getRecommendedPercentage();
            
            if (excessPercentage > 10) {
                BigDecimal potentialSavings = analysis.getTotalExpenses()
                    .multiply(BigDecimal.valueOf(excessPercentage / 100))
                    .multiply(BigDecimal.valueOf(0.2)); // Suggest 20% reduction
                
                recommendations.add(RecommendationDTO.builder()
                    .message(String.format("Your %s expenses are %.1f%% of your budget - %.1f%% above recommended. " +
                        "You could save $%.2f per month by reducing %s spending by 20%%.",
                        category.getCategoryDisplayName(),
                        category.getPercentage(),
                        excessPercentage,
                        potentialSavings,
                        category.getCategoryDisplayName().toLowerCase()))
                    .priority(priority++)
                    .category(category.getCategory())
                    .source("Rule-Based")
                    .actionable(true)
                    .potentialSavings(potentialSavings)
                    .build());
            }
        }
        
        // Category-specific tips
        recommendations.addAll(generateCategorySpecificTips(analysis));
        
        // Sort by priority and limit to top 5
        return recommendations.stream()
            .sorted(Comparator.comparing(RecommendationDTO::getPriority))
            .limit(5)
            .collect(Collectors.toList());
    }
    
    /**
     * Generate category-specific saving tips
     */
    private List<RecommendationDTO> generateCategorySpecificTips(SpendingAnalysisDTO analysis) {
        List<RecommendationDTO> tips = new ArrayList<>();
        
        for (CategoryAnalysisDTO category : analysis.getCategoryBreakdown()) {
            if (category.getPercentage() > 20) { // Only for significant categories
                String tip = switch (TransactionCategory.valueOf(category.getCategory())) {
                    case FOOD -> "Try meal planning and cooking at home 4-5 days a week to reduce food expenses significantly.";
                    case ENTERTAINMENT -> "Look for free entertainment alternatives like parks, libraries, and community events.";
                    case SHOPPING -> "Implement a 48-hour rule: wait 48 hours before making non-essential purchases.";
                    case TRANSPORT -> "Consider carpooling, public transport, or combining trips to reduce transportation costs.";
                    case GROCERIES -> "Use shopping lists and buy generic brands to save 20-30% on grocery bills.";
                    case HEALTH -> "Check if your insurance covers preventive care visits and generic medications.";
                    default -> null;
                };
                
                if (tip != null) {
                    tips.add(RecommendationDTO.builder()
                        .message(tip)
                        .priority(10) // Lower priority for general tips
                        .category(category.getCategory())
                        .source("Rule-Based")
                        .actionable(true)
                        .build());
                }
            }
        }
        
        return tips;
    }
}
