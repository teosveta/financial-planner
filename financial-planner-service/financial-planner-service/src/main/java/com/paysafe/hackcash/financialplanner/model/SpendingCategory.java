package com.paysafe.hackcash.financialplanner.model;

public enum SpendingCategory {
    FOOD("Food & Dining", "🍽️"),
    TRAVEL("Travel & Transportation", "✈️"),
    BILLS("Bills & Utilities", "📄"),
    ENTERTAINMENT("Entertainment", "🎬"),
    SHOPPING("Shopping", "🛍️"),
    HEALTH("Health & Wellness", "🏥"),
    TRANSPORT("Local Transport", "🚗"),
    TRANSFER("Transfers", "💸"),
    INCOME("Income", "💰"),
    OTHER("Other", "📦");
    
    private final String displayName;
    private final String emoji;
    
    SpendingCategory(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getFullDisplay() {
        return emoji + " " + displayName;
    }
}
