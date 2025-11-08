package com.financialplanner.service;

import com.financialplanner.dto.TransactionDTO;
import com.financialplanner.model.Transaction.TransactionCategory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AIRecommendationEngine {

    // Industry average spending percentages
    private static final Map<TransactionCategory, Double> AVERAGE_PERCENTAGES = Map.of(
            TransactionCategory.FOOD, 30.0,
            TransactionCategory.TRANSPORT, 15.0,
            TransactionCategory.BILLS, 25.0,
            TransactionCategory.ENTERTAINMENT, 10.0,
            TransactionCategory.SHOPPING, 15.0,
            TransactionCategory.HEALTH, 5.0
    );

    // Threshold for recommendations (5% difference from average)
    private static final double THRESHOLD_PERCENTAGE = 5.0;

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

        // Analyze each category
        for (TransactionDTO.CategoryStats stats : categoryBreakdown) {
            analyzeCategory(stats, totalExpenses, recommendations);
        }

        // Add general recommendations
        addGeneralRecommendations(categoryBreakdown, totalExpenses, recommendations);

        // If no specific issues found, add positive feedback
        if (recommendations.isEmpty()) {
            recommendations.add("Great job! Your spending is well-balanced across categories.");
            recommendations.add("Consider setting aside 20% of your income for savings and investments.");
        }

        return recommendations;
    }

    private void analyzeCategory(
            TransactionDTO.CategoryStats stats,
            BigDecimal totalExpenses,
            List<String> recommendations) {

        TransactionCategory category = stats.getCategory();
        Double userPercentage = stats.getPercentage();
        Double averagePercentage = AVERAGE_PERCENTAGES.getOrDefault(category, 0.0);

        if (averagePercentage == 0.0) {
            return; // Skip categories without average data
        }

        double difference = userPercentage - averagePercentage;

        // High spending alert
        if (difference > THRESHOLD_PERCENTAGE) {
            String recommendation = generateHighSpendingRecommendation(
                    category, userPercentage, averagePercentage, stats.getTotalAmount()
            );
            recommendations.add(recommendation);
        }

        // Low spending in essential categories (potential red flag)
        if (category == TransactionCategory.HEALTH && userPercentage < 3.0) {
            recommendations.add("💡 Your health spending is quite low. Don't neglect preventive care and wellness!");
        }
    }

    private String generateHighSpendingRecommendation(
            TransactionCategory category,
            Double userPercentage,
            Double averagePercentage,
            BigDecimal totalAmount) {

        double percentageAbove = userPercentage - averagePercentage;
        double reductionTarget = 20.0; // Suggest 20% reduction

        BigDecimal potentialSavings = totalAmount
                .multiply(BigDecimal.valueOf(reductionTarget / 100))
                .setScale(2, RoundingMode.HALF_UP);

        return switch (category) {
            case FOOD -> String.format(
                    "🍔 Your food expenses are %.1f%% of your budget - %.1f%% above average. " +
                    "Try meal prepping or cooking at home more often. You could save $%.2f per month by reducing food spending by 20%%.",
                    userPercentage, percentageAbove, potentialSavings
            );
            case ENTERTAINMENT -> String.format(
                    "🎬 Your entertainment expenses are %.1f%% of your budget - %.1f%% above average. " +
                    "Consider using free alternatives or reducing subscriptions. Potential monthly savings: $%.2f (20%% reduction).",
                    userPercentage, percentageAbove, potentialSavings
            );
            case SHOPPING -> String.format(
                    "🛍️ Your shopping expenses are %.1f%% of your budget - %.1f%% above average. " +
                    "Try the 24-hour rule before purchases. You could save $%.2f monthly by cutting non-essential shopping by 20%%.",
                    userPercentage, percentageAbove, potentialSavings
            );
            case TRANSPORT -> String.format(
                    "🚗 Your transport costs are %.1f%% of your budget - %.1f%% above average. " +
                    "Consider carpooling, public transit, or biking. Potential savings: $%.2f per month.",
                    userPercentage, percentageAbove, potentialSavings
            );
            case BILLS -> String.format(
                    "📱 Your bills are %.1f%% of your budget - %.1f%% above average. " +
                    "Review subscriptions and negotiate rates with providers. You could save $%.2f monthly.",
                    userPercentage, percentageAbove, potentialSavings
            );
            default -> String.format(
                    "💰 Your %s expenses are %.1f%% above average. Consider reviewing this category for savings opportunities.",
                    category.getDisplayName(), percentageAbove
            );
        };
    }

    private void addGeneralRecommendations(
            List<TransactionDTO.CategoryStats> categoryBreakdown,
            BigDecimal totalExpenses,
            List<String> recommendations) {

        // Find highest spending category
        TransactionDTO.CategoryStats highestCategory = categoryBreakdown.stream()
                .max((a, b) -> a.getTotalAmount().compareTo(b.getTotalAmount()))
                .orElse(null);

        if (highestCategory != null && recommendations.isEmpty()) {
            recommendations.add(String.format(
                    "💡 Your highest spending category is %s at $%.2f (%.1f%%). " +
                    "This is your best opportunity for significant savings.",
                    highestCategory.getCategoryDisplayName(),
                    highestCategory.getTotalAmount(),
                    highestCategory.getPercentage()
            ));
        }

        // Budget recommendation
        if (totalExpenses.compareTo(BigDecimal.valueOf(1000)) > 0) {
            recommendations.add(
                    "📊 Pro tip: Follow the 50/30/20 rule - 50% needs, 30% wants, 20% savings/debt repayment."
            );
        }
    }

    /**
     * Calculate potential monthly savings across all categories
     */
    public BigDecimal calculateTotalPotentialSavings(List<TransactionDTO.CategoryStats> categoryBreakdown) {
        return categoryBreakdown.stream()
                .filter(stats -> {
                    Double avg = AVERAGE_PERCENTAGES.getOrDefault(stats.getCategory(), 0.0);
                    return stats.getPercentage() > avg + THRESHOLD_PERCENTAGE;
                })
                .map(stats -> stats.getTotalAmount().multiply(BigDecimal.valueOf(0.20)))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
