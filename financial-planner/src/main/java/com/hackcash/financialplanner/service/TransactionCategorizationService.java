package com.hackcash.financialplanner.service;

import com.hackcash.financialplanner.model.Transaction;
import com.hackcash.financialplanner.model.TransactionCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Transaction Categorization Service
 * Primary: OpenAI AI categorization
 * Fallback: Rule-based pattern matching
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionCategorizationService {
    
    private final OpenAIService openAIService;
    
    // Fallback rules - regex patterns for merchant/description matching
    private static final Map<TransactionCategory, Pattern[]> CATEGORIZATION_RULES = new HashMap<>();
    
    static {
        CATEGORIZATION_RULES.put(TransactionCategory.FOOD, new Pattern[]{
            Pattern.compile("restaurant|cafe|coffee|pizza|burger|mcdonalds|kfc|subway|starbucks|dunkin|food", Pattern.CASE_INSENSITIVE),
            Pattern.compile("dining|lunch|dinner|breakfast|takeout|delivery|ubereats|doordash", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.GROCERIES, new Pattern[]{
            Pattern.compile("grocery|supermarket|walmart|target|costco|kroger|safeway|whole foods|trader joes", Pattern.CASE_INSENSITIVE),
            Pattern.compile("market|fresh|produce|organic", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.TRANSPORT, new Pattern[]{
            Pattern.compile("uber|lyft|taxi|cab|metro|subway|bus|train|transit|parking|toll", Pattern.CASE_INSENSITIVE),
            Pattern.compile("gas station|fuel|shell|chevron|exxon|bp|mobil", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.TRAVEL, new Pattern[]{
            Pattern.compile("airline|flight|hotel|airbnb|booking|expedia|travel|vacation|resort", Pattern.CASE_INSENSITIVE),
            Pattern.compile("airport|luggage|rental car|hertz|enterprise|avis", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.ENTERTAINMENT, new Pattern[]{
            Pattern.compile("netflix|spotify|hulu|disney|cinema|movie|theater|concert|game|gaming|steam", Pattern.CASE_INSENSITIVE),
            Pattern.compile("entertainment|ticket|event|show|museum|amusement|xbox|playstation", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.SHOPPING, new Pattern[]{
            Pattern.compile("amazon|ebay|mall|shop|store|retail|clothing|fashion|nike|adidas|zara|h&m", Pattern.CASE_INSENSITIVE),
            Pattern.compile("electronics|bestbuy|apple store|purchase|order", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.BILLS, new Pattern[]{
            Pattern.compile("utility|electric|water|gas bill|phone bill|internet|cable|verizon|at&t|comcast", Pattern.CASE_INSENSITIVE),
            Pattern.compile("insurance|rent|mortgage|subscription|membership|payment", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.HEALTH, new Pattern[]{
            Pattern.compile("pharmacy|cvs|walgreens|hospital|doctor|clinic|medical|health|gym|fitness|yoga", Pattern.CASE_INSENSITIVE),
            Pattern.compile("prescription|medicine|dental|vision|therapy|wellness", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.EDUCATION, new Pattern[]{
            Pattern.compile("school|university|college|tuition|course|class|books|textbook|learning|udemy", Pattern.CASE_INSENSITIVE),
            Pattern.compile("education|training|workshop|seminar|academy", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.INCOME, new Pattern[]{
            Pattern.compile("salary|paycheck|wage|bonus|income|deposit|payment received|refund", Pattern.CASE_INSENSITIVE),
            Pattern.compile("transfer in|credit|interest earned", Pattern.CASE_INSENSITIVE)
        });
        
        CATEGORIZATION_RULES.put(TransactionCategory.SAVINGS, new Pattern[]{
            Pattern.compile("savings|investment|stock|bond|crypto|etf|mutual fund|401k|ira", Pattern.CASE_INSENSITIVE),
            Pattern.compile("investing|dividend|portfolio", Pattern.CASE_INSENSITIVE)
        });
    }
    
    /**
     * Categorize transaction using AI with fallback to rules
     */
    public TransactionCategory categorize(Transaction transaction) {
        // Try AI categorization first
        try {
            String aiCategory = openAIService.categorizeTransaction(
                transaction.getMerchantName(),
                transaction.getDescription(),
                transaction.getAmount().doubleValue()
            );
            
            if (aiCategory != null && !aiCategory.isEmpty()) {
                TransactionCategory category = TransactionCategory.fromString(aiCategory);
                if (category != TransactionCategory.OTHER) {
                    log.info("AI categorized '{}' as {}", transaction.getMerchantName(), category);
                    transaction.setAiCategorization(aiCategory);
                    transaction.setConfidenceScore(0.85); // High confidence for AI
                    return category;
                }
            }
        } catch (Exception e) {
            log.warn("AI categorization failed, using fallback: {}", e.getMessage());
        }
        
        // Fallback to rule-based categorization
        TransactionCategory category = categorizeByRules(transaction);
        transaction.setConfidenceScore(0.65); // Lower confidence for rules
        log.info("Rule-based categorized '{}' as {}", transaction.getMerchantName(), category);
        
        return category;
    }
    
    /**
     * Rule-based categorization fallback
     */
    private TransactionCategory categorizeByRules(Transaction transaction) {
        String searchText = (transaction.getMerchantName() + " " + 
                            (transaction.getDescription() != null ? transaction.getDescription() : "")).toLowerCase();
        
        // Check each category's patterns
        for (Map.Entry<TransactionCategory, Pattern[]> entry : CATEGORIZATION_RULES.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(searchText).find()) {
                    return entry.getKey();
                }
            }
        }
        
        // Default to OTHER if no match
        return TransactionCategory.OTHER;
    }
    
    /**
     * Recategorize existing transaction (for user corrections or AI improvements)
     */
    public TransactionCategory recategorize(Transaction transaction) {
        log.info("Recategorizing transaction: {}", transaction.getId());
        return categorize(transaction);
    }
}
