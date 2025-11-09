package com.financialplanner.service;

import com.financialplanner.model.Transaction.TransactionCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCategorizationService {

    private final Map<Pattern, TransactionCategory> categorizationRules;
    private final ClaudeAIService claudeAIService;

    public TransactionCategorizationService(ClaudeAIService claudeAIService) {
        this.claudeAIService = claudeAIService;
        this.categorizationRules = initializeRules();
    }

    /**
     * Categorizes a transaction using AI first, then falls back to rule-based categorization
     */
    public TransactionCategory categorize(String merchantName, String description) {
        return categorize(merchantName, description, BigDecimal.ZERO);
    }

    /**
     * Categorizes a transaction based on merchant name, description, and amount
     * Uses AI when available, otherwise falls back to rule-based categorization
     */
    public TransactionCategory categorize(String merchantName, String description, BigDecimal amount) {
        // Try AI categorization first
        if (claudeAIService.isAvailable()) {
            try {
                String aiCategory = claudeAIService.categorizeTransaction(merchantName, description, amount);
                if (aiCategory != null) {
                    TransactionCategory category = TransactionCategory.valueOf(aiCategory);
                    log.info("AI categorized '{}' as {}", merchantName, category);
                    return category;
                }
            } catch (Exception e) {
                log.warn("AI categorization failed, falling back to rule-based: {}", e.getMessage());
            }
        }

        // Fall back to rule-based categorization
        return categorizeByRules(merchantName, description);
    }

    /**
     * Rule-based categorization (fallback when AI is not available)
     */
    private TransactionCategory categorizeByRules(String merchantName, String description) {
        String searchText = (merchantName + " " + description).toLowerCase();

        // Check each pattern for a match
        for (Map.Entry<Pattern, TransactionCategory> entry : categorizationRules.entrySet()) {
            if (entry.getKey().matcher(searchText).find()) {
                log.debug("Rule-based categorization: '{}' matched as {}", merchantName, entry.getValue());
                return entry.getValue();
            }
        }

        log.debug("No rule match found for '{}', defaulting to OTHER", merchantName);
        return TransactionCategory.OTHER;
    }

    /**
     * Initialize categorization rules with merchant patterns
     * This approach is easily extensible - add new rules as needed
     */
    private Map<Pattern, TransactionCategory> initializeRules() {
        Map<Pattern, TransactionCategory> rules = new HashMap<>();

        // FOOD
        rules.put(compilePattern("restaurant|cafe|coffee|pizza|burger|mcdonald|starbucks|domino|subway|food|grocery|supermarket|whole foods|trader joe|safeway|kroger"), 
                  TransactionCategory.FOOD);

        // TRAVEL
        rules.put(compilePattern("hotel|airline|flight|airbnb|booking\\.com|expedia|travel|vacation|resort|hostel"), 
                  TransactionCategory.TRAVEL);

        // BILLS
        rules.put(compilePattern("electric|water|gas|utility|internet|phone|mobile|verizon|at&t|t-mobile|comcast|xfinity|spectrum|insurance"), 
                  TransactionCategory.BILLS);

        // ENTERTAINMENT
        rules.put(compilePattern("netflix|spotify|hulu|disney|amazon prime|cinema|movie|theater|theatre|concert|event|game|xbox|playstation|steam"), 
                  TransactionCategory.ENTERTAINMENT);

        // SHOPPING
        rules.put(compilePattern("amazon|ebay|walmart|target|best buy|mall|clothing|fashion|electronics|furniture|ikea|home depot|lowes"), 
                  TransactionCategory.SHOPPING);

        // HEALTH
        rules.put(compilePattern("pharmacy|cvs|walgreens|hospital|clinic|doctor|dentist|medical|health|gym|fitness|yoga|wellness"), 
                  TransactionCategory.HEALTH);

        // TRANSPORT
        rules.put(compilePattern("uber|lyft|taxi|cab|bus|train|metro|subway|parking|fuel|gas station|shell|chevron|exxon|bp"), 
                  TransactionCategory.TRANSPORT);

        return rules;
    }

    private Pattern compilePattern(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }
}
