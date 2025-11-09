package com.hackcash.financialplanner.model;

import lombok.Getter;

@Getter
public enum TransactionCategory {
    FOOD("Food & Dining", "🍔", 30.0),
    TRAVEL("Travel & Transportation", "✈️", 15.0),
    BILLS("Bills & Utilities", "📄", 25.0),
    ENTERTAINMENT("Entertainment", "🎬", 10.0),
    SHOPPING("Shopping", "🛍️", 15.0),
    HEALTH("Health & Fitness", "💊", 8.0),
    TRANSPORT("Transport", "🚗", 12.0),
    EDUCATION("Education", "📚", 5.0),
    GROCERIES("Groceries", "🛒", 20.0),
    INCOME("Income", "💰", 0.0),
    SAVINGS("Savings & Investment", "💎", 0.0),
    OTHER("Other", "📦", 5.0);
    
    private final String displayName;
    private final String icon;
    private final Double recommendedPercentage; // Benchmark for recommendations
    
    TransactionCategory(String displayName, String icon, Double recommendedPercentage) {
        this.displayName = displayName;
        this.icon = icon;
        this.recommendedPercentage = recommendedPercentage;
    }
    
    public static TransactionCategory fromString(String text) {
        for (TransactionCategory category : TransactionCategory.values()) {
            if (category.name().equalsIgnoreCase(text) || 
                category.displayName.equalsIgnoreCase(text)) {
                return category;
            }
        }
        return OTHER;
    }
}
