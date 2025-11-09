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
     * Build an intelligent prompt for Claude AI with enhanced financial analysis capabilities
     */
    private String buildFinancialPrompt(BigDecimal totalExpenses, List<TransactionDTO.CategoryStats> categoryBreakdown) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are Claude, an expert financial advisor AI powered by Anthropic. Analyze this spending data with deep financial insights.\n\n");
        
        prompt.append("📊 SPENDING SUMMARY:\n");
        prompt.append("Total Monthly Expenses: $").append(totalExpenses.setScale(2, RoundingMode.HALF_UP)).append("\n\n");
        
        prompt.append("📈 DETAILED CATEGORY BREAKDOWN:\n");
        for (TransactionDTO.CategoryStats stats : categoryBreakdown) {
            prompt.append(String.format("• %s: $%.2f (%.1f%% of budget, %d transactions, avg $%.2f per transaction)\n",
                    stats.getCategoryDisplayName(),
                    stats.getTotalAmount(),
                    stats.getPercentage(),
                    stats.getTransactionCount(),
                    stats.getTransactionCount() > 0 ? stats.getTotalAmount().doubleValue() / stats.getTransactionCount() : 0));
        }
        
        prompt.append("\n🎯 INDUSTRY BENCHMARKS (for comparison):\n");
        prompt.append("• Food & Dining: 25-30% (healthy range)\n");
        prompt.append("• Transportation: 12-18%\n");
        prompt.append("• Bills & Utilities: 20-25%\n");
        prompt.append("• Entertainment: 8-12%\n");
        prompt.append("• Shopping: 10-15%\n");
        prompt.append("• Health & Wellness: 5-10%\n");
        prompt.append("• Travel: 5-10%\n\n");
        
        prompt.append("💡 YOUR ANALYSIS SHOULD:\n");
        prompt.append("1. Identify spending patterns and anomalies compared to industry benchmarks\n");
        prompt.append("2. Calculate realistic monthly savings (15-25% reduction in overspending categories)\n");
        prompt.append("3. Provide SPECIFIC, ACTIONABLE strategies (not generic advice)\n");
        prompt.append("4. Acknowledge positive financial habits\n");
        prompt.append("5. Prioritize high-impact savings opportunities\n");
        prompt.append("6. Consider behavioral psychology in your recommendations\n");
        prompt.append("7. Suggest automation or tools where applicable\n\n");
        
        prompt.append("✨ FORMAT REQUIREMENTS:\n");
        prompt.append("- Provide exactly 4-6 recommendations\n");
        prompt.append("- Start each with a relevant emoji (🍔 💰 💡 ✨ 🎯 🚀 ⚡ 📱)\n");
        prompt.append("- Include specific dollar amounts and percentages\n");
        prompt.append("- Be conversational yet professional\n");
        prompt.append("- Each recommendation: 1-2 sentences, under 160 characters\n");
        prompt.append("- Focus on immediate, practical actions\n\n");
        
        prompt.append("📝 EXAMPLE RESPONSES:\n");
        prompt.append("🍔 Food spending at $520/month (38%) is high. Meal prep Sundays + Thursday could save $120-150/month.\n");
        prompt.append("💡 Utilities at $180 (12%) are excellent! Consider smart plugs to save another $15-20/month.\n");
        prompt.append("🎯 Entertainment $85 (6%) is well-managed. This is sustainable long-term - great job!\n");
        prompt.append("🚀 Transport $240 (17%) is above average. Try carpooling 2 days/week for $60 monthly savings.\n\n");
        
        prompt.append("🤖 NOW ANALYZE THE USER'S DATA AND PROVIDE YOUR EXPERT CLAUDE AI RECOMMENDATIONS:\n");
        
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
