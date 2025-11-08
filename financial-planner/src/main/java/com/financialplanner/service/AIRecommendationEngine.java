package com.financialplanner.service;

import com.financialplanner.dto.TransactionDTO;
import com.financialplanner.model.Transaction.TransactionCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationEngine {

    private final ClaudeAIService claudeAIService;

    /**
     * Generate personalized AI recommendations based on spending patterns
     */
    public List<String> generateRecommendations(
            BigDecimal totalExpenses,
            List<TransactionDTO.CategoryStats> categoryBreakdown) {

        List<String> recommendations = new ArrayList<>();

        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            recommendations.add("No transactions recorded yet. Start adding your expenses to get personalized insights!");
            return recommendations;
        }

        // Check if AI is available
        boolean aiAvailable = claudeAIService.isAvailable();
        
        if (!aiAvailable) {
            log.warn("Claude AI service not available, using fallback recommendations");
            return generateFallbackRecommendations(totalExpenses, categoryBreakdown);
        }

        try {
            // Build intelligent prompt for AI
            String prompt = buildFinancialPrompt(totalExpenses, categoryBreakdown);
            
            log.info("Generating AI recommendations with Claude for ${} total expenses", totalExpenses);
            
            // Get AI response from Claude
            String aiResponse = claudeAIService.generateRecommendations(prompt);
            
            // Parse AI response into recommendations
            recommendations = parseAIResponse(aiResponse);
            
            if (recommendations.isEmpty()) {
                log.warn("AI returned empty recommendations, using fallback");
                return generateFallbackRecommendations(totalExpenses, categoryBreakdown);
            }
            
            log.info("Generated {} AI recommendations", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("Error generating AI recommendations: {}", e.getMessage(), e);
            return generateFallbackRecommendations(totalExpenses, categoryBreakdown);
        }
    }

    /**
     * Build an intelligent prompt for the AI model
     */
    private String buildFinancialPrompt(BigDecimal totalExpenses, List<TransactionDTO.CategoryStats> categoryBreakdown) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a financial advisor AI. Analyze this spending data and provide 3-5 specific, actionable recommendations.\n\n");
        prompt.append("SPENDING SUMMARY:\n");
        prompt.append("Total Expenses: $").append(totalExpenses.setScale(2, RoundingMode.HALF_UP)).append("\n\n");
        
        prompt.append("CATEGORY BREAKDOWN:\n");
        for (TransactionDTO.CategoryStats stats : categoryBreakdown) {
            prompt.append(String.format("- %s: $%.2f (%.1f%%, %d transactions)\n",
                    stats.getCategoryDisplayName(),
                    stats.getTotalAmount(),
                    stats.getPercentage(),
                    stats.getTransactionCount()));
        }
        
        prompt.append("\nINDUSTRY AVERAGES (for comparison):\n");
        prompt.append("- Food: 30%\n");
        prompt.append("- Transport: 15%\n");
        prompt.append("- Bills: 25%\n");
        prompt.append("- Entertainment: 10%\n");
        prompt.append("- Shopping: 15%\n");
        prompt.append("- Health: 5%\n");
        
        prompt.append("\nPROVIDE:\n");
        prompt.append("1. Identify categories where spending is significantly above industry average\n");
        prompt.append("2. Calculate potential monthly savings (suggest 15-20% reduction where overspending)\n");
        prompt.append("3. Give specific, actionable advice (e.g., 'meal prep 3 days/week' not 'reduce food spending')\n");
        prompt.append("4. Mention positive patterns if spending is well-balanced\n");
        prompt.append("5. Prioritize the biggest savings opportunities\n\n");
        
        prompt.append("FORMAT YOUR RESPONSE AS:\n");
        prompt.append("- Start each recommendation with an emoji (🍔 💰 💡 ✨ 🎯)\n");
        prompt.append("- One recommendation per line\n");
        prompt.append("- Be specific with dollar amounts\n");
        prompt.append("- Keep each recommendation under 150 characters\n");
        prompt.append("- Focus on practical, immediate actions\n\n");
        
        prompt.append("EXAMPLE OUTPUT:\n");
        prompt.append("🍔 Your food spending is $450/month (40% of budget). Try meal prepping 3x/week to save $90/month.\n");
        prompt.append("💰 Entertainment at $150 is reasonable. Consider free alternatives 1x/month for $15 savings.\n");
        prompt.append("✨ Great job keeping health expenses at 4% - that's below average and sustainable!\n\n");
        
        prompt.append("NOW ANALYZE THE DATA ABOVE AND PROVIDE YOUR RECOMMENDATIONS:\n");
        
        return prompt.toString();
    }

    /**
     * Parse AI response into a list of recommendations
     */
    private List<String> parseAIResponse(String aiResponse) {
        List<String> recommendations = new ArrayList<>();
        
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            return recommendations;
        }
        
        // Split by newlines and filter out empty lines
        String[] lines = aiResponse.split("\n");
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines and section headers
            if (line.isEmpty() || 
                line.startsWith("RECOMMENDATIONS:") ||
                line.startsWith("Based on") ||
                line.startsWith("Here are") ||
                line.length() < 10) {
                continue;
            }
            
            // Clean up common AI prefixes
            line = line.replaceAll("^\\d+\\.\\s*", ""); // Remove "1. ", "2. ", etc.
            line = line.replaceAll("^-\\s*", ""); // Remove "- "
            line = line.replaceAll("^\\*\\s*", ""); // Remove "* "
            
            if (!line.isEmpty() && line.length() > 10) {
                recommendations.add(line);
            }
        }
        
        // Limit to 5 recommendations
        if (recommendations.size() > 5) {
            recommendations = recommendations.subList(0, 5);
        }
        
        return recommendations;
    }

    /**
     * Fallback recommendations when AI is not available
     */
    private List<String> generateFallbackRecommendations(
            BigDecimal totalExpenses,
            List<TransactionDTO.CategoryStats> categoryBreakdown) {
        
        List<String> recommendations = new ArrayList<>();
        
        // Industry averages for comparison
        var averages = java.util.Map.of(
                TransactionCategory.FOOD, 30.0,
                TransactionCategory.TRANSPORT, 15.0,
                TransactionCategory.BILLS, 25.0,
                TransactionCategory.ENTERTAINMENT, 10.0,
                TransactionCategory.SHOPPING, 15.0,
                TransactionCategory.HEALTH, 5.0
        );

        for (TransactionDTO.CategoryStats stats : categoryBreakdown) {
            Double avg = averages.getOrDefault(stats.getCategory(), 0.0);
            if (avg == 0.0) continue;

            double difference = stats.getPercentage() - avg;

            if (difference > 5.0) {
                BigDecimal savings = stats.getTotalAmount()
                        .multiply(BigDecimal.valueOf(0.20))
                        .setScale(2, RoundingMode.HALF_UP);

                String emoji = switch (stats.getCategory()) {
                    case FOOD -> "🍔";
                    case ENTERTAINMENT -> "🎬";
                    case SHOPPING -> "🛍️";
                    case TRANSPORT -> "🚗";
                    case BILLS -> "💡";
                    default -> "💰";
                };

                recommendations.add(String.format(
                        "%s Your %s expenses are %.1f%% of budget (%.1f%% above average). " +
                                "Reducing by 20%% could save $%.2f/month.",
                        emoji,
                        stats.getCategoryDisplayName().toLowerCase(),
                        stats.getPercentage(),
                        difference,
                        savings
                ));
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✨ Great job! Your spending is well-balanced across categories.");
            recommendations.add("💡 Pro tip: Aim for the 50/30/20 rule - 50% needs, 30% wants, 20% savings.");
        }

        recommendations.add("🤖 Note: AI recommendations are currently using fallback mode. Configure Claude API key for personalized AI insights!");

        return recommendations;
    }

    /**
     * Calculate total potential savings across all categories
     */
    public BigDecimal calculateTotalPotentialSavings(List<TransactionDTO.CategoryStats> categoryBreakdown) {
        var averages = java.util.Map.of(
                TransactionCategory.FOOD, 30.0,
                TransactionCategory.TRANSPORT, 15.0,
                TransactionCategory.BILLS, 25.0,
                TransactionCategory.ENTERTAINMENT, 10.0,
                TransactionCategory.SHOPPING, 15.0,
                TransactionCategory.HEALTH, 5.0
        );

        return categoryBreakdown.stream()
                .filter(stats -> {
                    Double avg = averages.getOrDefault(stats.getCategory(), 0.0);
                    return stats.getPercentage() > avg + 5.0;
                })
                .map(stats -> stats.getTotalAmount().multiply(BigDecimal.valueOf(0.20)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
