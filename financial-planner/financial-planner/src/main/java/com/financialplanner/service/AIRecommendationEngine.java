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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRecommendationEngine {

    private final OpenAIService openAIService;
    private static final Map<TransactionCategory, Double> BENCHMARK_PERCENTAGES = Map.of(
            TransactionCategory.FOOD, 30.0,
            TransactionCategory.TRANSPORT, 15.0,
            TransactionCategory.BILLS, 25.0,
            TransactionCategory.ENTERTAINMENT, 10.0,
            TransactionCategory.SHOPPING, 15.0,
            TransactionCategory.HEALTH, 7.0,
            TransactionCategory.TRAVEL, 8.0,
            TransactionCategory.OTHER, 5.0
    );
    private static final double ALERT_THRESHOLD_PERCENT = 5.0;
    private static final double SAVINGS_TARGET_RATIO = 0.20;

    /**
     * Generate personalized AI recommendations based on spending patterns
     */
    public List<String> generateRecommendations(
            BigDecimal totalExpenses,
            BigDecimal previousPeriodExpenses,
            List<TransactionDTO.CategoryStats> categoryBreakdown) {

        List<String> recommendations = new ArrayList<>();

        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            recommendations.add("No transactions recorded yet. Start adding your expenses to get personalized insights!");
            return recommendations;
        }

        // Check if AI is available
        boolean aiAvailable = openAIService.isAvailable();

        if (!aiAvailable) {
            log.warn("OpenAI service not available, using fallback recommendations");
            return generateFallbackRecommendations(totalExpenses, previousPeriodExpenses, categoryBreakdown);
        }

        try {
            // Build intelligent prompt for AI
            String prompt = buildFinancialPrompt(totalExpenses, previousPeriodExpenses, categoryBreakdown);

            log.info("Generating AI recommendations with OpenAI for total expenses {}", totalExpenses);

            // Get AI response from OpenAI
            String aiResponse = openAIService.generateRecommendations(prompt);

            // Parse AI response into recommendations
            recommendations = parseAIResponse(aiResponse);

            if (recommendations.isEmpty()) {
                log.warn("AI returned empty recommendations, using fallback");
                return generateFallbackRecommendations(totalExpenses, previousPeriodExpenses, categoryBreakdown);
            }

            log.info("Generated {} AI recommendations", recommendations.size());
            return recommendations;

        } catch (Exception e) {
            log.error("Error generating AI recommendations: {}", e.getMessage(), e);
            return generateFallbackRecommendations(totalExpenses, previousPeriodExpenses, categoryBreakdown);
        }
    }

    /**
     * Build an intelligent prompt for OpenAI with enhanced financial analysis capabilities
     */
    private String buildFinancialPrompt(
            BigDecimal totalExpenses,
            BigDecimal previousPeriodExpenses,
            List<TransactionDTO.CategoryStats> categoryBreakdown) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an expert financial coach. Review the user's spending data and provide concise, actionable savings recommendations.\n\n");

        prompt.append("📊 SPENDING SUMMARY:\n");
        prompt.append("Current Period Total: $").append(totalExpenses.setScale(2, RoundingMode.HALF_UP)).append("\n");
        if (previousPeriodExpenses != null && previousPeriodExpenses.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal difference = totalExpenses.subtract(previousPeriodExpenses);
            double pctChange = difference.divide(previousPeriodExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            prompt.append(String.format("Previous Period Total: $%.2f (%+.1f%% change)\n",
                    previousPeriodExpenses.setScale(2, RoundingMode.HALF_UP),
                    pctChange));
        } else {
            prompt.append("Previous Period Total: No historical data\n");
        }
        prompt.append("\n");

        prompt.append("📈 DETAILED CATEGORY BREAKDOWN:\n");
        for (TransactionDTO.CategoryStats stats : categoryBreakdown) {
            prompt.append(String.format("• %s: $%.2f (%.1f%% of spend, %d txns, avg $%.2f)",
                    stats.getCategoryDisplayName(),
                    stats.getTotalAmount(),
                    stats.getPercentage(),
                    stats.getTransactionCount(),
                    stats.getTransactionCount() > 0 ? stats.getTotalAmount().doubleValue() / Math.max(stats.getTransactionCount(), 1) : 0));

            Double benchmark = BENCHMARK_PERCENTAGES.get(stats.getCategory());
            if (benchmark != null) {
                prompt.append(String.format(" | Benchmark %.0f%%", benchmark));
            }
            if (stats.getPreviousPeriodTotal() != null && stats.getPreviousPeriodTotal().compareTo(BigDecimal.ZERO) > 0) {
                prompt.append(String.format(" | Prev $%.2f (%+.1f%%)",
                        stats.getPreviousPeriodTotal().setScale(2, RoundingMode.HALF_UP),
                        stats.getPercentageChange()));
            }
            prompt.append("\n");
        }

        prompt.append("\n🎯 INDUSTRY BENCHMARKS (for comparison):\n");
        BENCHMARK_PERCENTAGES.forEach((category, pct) ->
                prompt.append(String.format("• %s: %s%%% of monthly spend\n",
                        category.getDisplayName(), pct.intValue())));
        prompt.append("\n");

        prompt.append("💡 YOUR ANALYSIS SHOULD:\n");
        prompt.append("1. Identify categories exceeding benchmarks or rising more than 10% vs previous period.\n");
        prompt.append("2. Highlight one category that is well-managed.\n");
        prompt.append("3. Suggest concrete monthly savings opportunities with dollar amounts (assume 15-20% reduction).\n");
        prompt.append("4. Keep output in plain English, no markdown headings.\n\n");

        prompt.append("✨ FORMAT REQUIREMENTS:\n");
        prompt.append("- Return 3 to 5 bullet-style sentences separated by newlines.\n");
        prompt.append("- Start each line with an emoji relevant to the category (e.g., 🍔, 🚗, 💡, 🎯).\n");
        prompt.append("- Mention current percentage of budget and the variance vs benchmark or previous period.\n");
        prompt.append("- Include estimated monthly savings in dollars when suggesting reductions.\n\n");

        prompt.append("End with insights only—no introductions or summaries.");

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
            BigDecimal previousPeriodExpenses,
            List<TransactionDTO.CategoryStats> categoryBreakdown) {
        
        List<String> recommendations = new ArrayList<>();

        for (TransactionDTO.CategoryStats stats : categoryBreakdown) {
            Double benchmark = BENCHMARK_PERCENTAGES.get(stats.getCategory());
            if (benchmark == null) {
                continue;
            }

            double variance = stats.getPercentage() - benchmark;
            boolean trendingUp = stats.getPercentageChange() != null && stats.getPercentageChange() > 10.0;

            if (variance > ALERT_THRESHOLD_PERCENT || trendingUp) {
                BigDecimal potentialSavings = stats.getTotalAmount()
                        .multiply(BigDecimal.valueOf(SAVINGS_TARGET_RATIO))
                        .setScale(2, RoundingMode.HALF_UP);

                String emoji = switch (stats.getCategory()) {
                    case FOOD -> "🍔";
                    case ENTERTAINMENT -> "🎬";
                    case SHOPPING -> "🛍️";
                    case TRANSPORT -> "🚗";
                    case BILLS -> "💡";
                    case HEALTH -> "💊";
                    case TRAVEL -> "✈️";
                    default -> "💰";
                };

                String trendMessage = trendingUp
                        ? String.format(", up %.1f%% vs last period", stats.getPercentageChange())
                        : "";

                recommendations.add(String.format(
                        "%s %s is %.1f%% of spend (%.1f%% above target%s). Cutting 20%% frees $%.2f each month.",
                        emoji,
                        stats.getCategoryDisplayName(),
                        stats.getPercentage(),
                        variance,
                        trendMessage,
                        potentialSavings
                ));
            }
        }

        if (recommendations.isEmpty()) {
            recommendations.add("✨ Spending is well-balanced this period—nice job staying near healthy benchmarks.");
            BigDecimal savingsTarget = totalExpenses.multiply(BigDecimal.valueOf(0.15))
                    .setScale(2, RoundingMode.HALF_UP);
            recommendations.add(String.format("💡 Consider auto-transferring $%.2f monthly to savings to lock in progress.",
                    savingsTarget));
        }

        return recommendations;
    }

    /**
     * Calculate total potential savings across all categories
     */
    public BigDecimal calculateTotalPotentialSavings(List<TransactionDTO.CategoryStats> categoryBreakdown) {
        return categoryBreakdown.stream()
                .filter(stats -> {
                    Double benchmark = BENCHMARK_PERCENTAGES.get(stats.getCategory());
                    return benchmark != null && stats.getPercentage() > benchmark + ALERT_THRESHOLD_PERCENT;
                })
                .map(stats -> stats.getTotalAmount().multiply(BigDecimal.valueOf(SAVINGS_TARGET_RATIO)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
