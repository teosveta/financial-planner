package com.paysafe.hackcash.financialplanner.service;

import com.paysafe.hackcash.financialplanner.dto.CategorizationResult;
import com.paysafe.hackcash.financialplanner.dto.FinancialAnalysisResponse;
import com.paysafe.hackcash.financialplanner.model.SpendingCategory;
import com.paysafe.hackcash.financialplanner.model.Transaction;
import com.paysafe.hackcash.financialplanner.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FinancialAnalysisService {
    
    private final TransactionRepository transactionRepository;
    private final AICategorizationService aiCategorizationService;
    private final AIRecommendationService aiRecommendationService;
    
    public FinancialAnalysisService(
            TransactionRepository transactionRepository,
            AICategorizationService aiCategorizationService,
            AIRecommendationService aiRecommendationService) {
        this.transactionRepository = transactionRepository;
        this.aiCategorizationService = aiCategorizationService;
        this.aiRecommendationService = aiRecommendationService;
    }
    
    /**
     * Generate comprehensive financial analysis for a user
     */
    public FinancialAnalysisResponse analyzeFinances(UUID userId, String period) {
        log.info("Starting financial analysis for user: {}, period: {}", userId, period);
        
        // Load and filter transactions
        List<Transaction> transactions = loadAndFilterTransactions(userId, period);
        
        if (transactions.isEmpty()) {
            log.warn("No transactions found for user: {}", userId);
            return createEmptyAnalysis(userId, period);
        }
        
        // Categorize transactions using AI
        categorizeTransactions(transactions);
        
        // Calculate summary statistics
        FinancialAnalysisResponse.AnalysisSummary summary = calculateSummary(transactions);
        
        // Calculate category breakdowns
        Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown = 
                calculateCategoryBreakdown(transactions, summary.getTotalSpent());
        
        // Generate AI recommendations
        List<FinancialAnalysisResponse.AIRecommendation> recommendations = 
                aiRecommendationService.generateRecommendations(categoryBreakdown, summary);
        
        // Generate insights
        List<FinancialAnalysisResponse.SpendingInsight> insights = 
                aiRecommendationService.generateInsights(categoryBreakdown, summary);
        
        // Analyze trends
        FinancialAnalysisResponse.TrendAnalysis trends = analyzeTrends(categoryBreakdown);
        
        log.info("Financial analysis completed successfully");
        
        return FinancialAnalysisResponse.builder()
                .userId(userId.toString())
                .period(period)
                .summary(summary)
                .categoryBreakdown(categoryBreakdown)
                .recommendations(recommendations)
                .insights(insights)
                .trends(trends)
                .build();
    }
    
    /**
     * Categorize all transactions in the system
     */
    public void categorizeAllTransactions() {
        log.info("Starting batch categorization of all transactions");
        
        List<Transaction> transactions = transactionRepository.loadTransactions();
        categorizeTransactions(transactions);
        transactionRepository.saveTransactions(transactions);
        
        log.info("Batch categorization completed for {} transactions", transactions.size());
    }
    
    private List<Transaction> loadAndFilterTransactions(UUID userId, String period) {
        List<Transaction> transactions = transactionRepository.loadTransactionsByUserId(userId);
        
        // Filter by time period
        LocalDateTime cutoffDate = calculateCutoffDate(period);
        
        return transactions.stream()
                .filter(tx -> tx.getCreatedOn() != null && tx.getCreatedOn().isAfter(cutoffDate))
                .filter(tx -> "SUCCEEDED".equals(tx.getStatus()))
                .collect(Collectors.toList());
    }
    
    private LocalDateTime calculateCutoffDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        
        return switch (period.toLowerCase()) {
            case "week", "weekly" -> now.minusWeeks(1);
            case "month", "monthly" -> now.minusMonths(1);
            case "quarter", "quarterly" -> now.minusMonths(3);
            case "year", "yearly" -> now.minusYears(1);
            default -> now.minusMonths(1); // Default to monthly
        };
    }
    
    private void categorizeTransactions(List<Transaction> transactions) {
        log.info("Categorizing {} transactions with AI", transactions.size());
        
        // Filter out already categorized transactions
        List<Transaction> uncategorized = transactions.stream()
                .filter(tx -> tx.getCategory() == null || tx.getCategory().isEmpty())
                .collect(Collectors.toList());
        
        if (uncategorized.isEmpty()) {
            log.info("All transactions already categorized");
            return;
        }
        
        // Batch categorize using AI
        List<CategorizationResult> results = aiCategorizationService.categorizeTransactions(uncategorized);
        
        // Apply categorization results
        for (int i = 0; i < uncategorized.size() && i < results.size(); i++) {
            Transaction tx = uncategorized.get(i);
            CategorizationResult result = results.get(i);
            
            tx.setCategory(result.getCategory().name());
            tx.setMerchantName(result.getMerchantName());
            tx.setConfidenceScore(result.getConfidenceScore());
        }
        
        log.info("Categorization complete: {} transactions processed", uncategorized.size());
    }
    
    private FinancialAnalysisResponse.AnalysisSummary calculateSummary(List<Transaction> transactions) {
        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        String currency = transactions.isEmpty() ? "EUR" : transactions.get(0).getCurrency();
        
        for (Transaction tx : transactions) {
            BigDecimal amount = tx.getAmount();
            
            if (isExpense(tx)) {
                totalSpent = totalSpent.add(amount);
            } else if (isIncome(tx)) {
                totalIncome = totalIncome.add(amount);
            }
            
            totalAmount = totalAmount.add(amount.abs());
        }
        
        BigDecimal netSavings = totalIncome.subtract(totalSpent);
        BigDecimal averageTransaction = transactions.isEmpty() ? BigDecimal.ZERO :
                totalAmount.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
        
        return FinancialAnalysisResponse.AnalysisSummary.builder()
                .totalSpent(totalSpent.setScale(2, RoundingMode.HALF_UP))
                .totalIncome(totalIncome.setScale(2, RoundingMode.HALF_UP))
                .netSavings(netSavings.setScale(2, RoundingMode.HALF_UP))
                .transactionCount(transactions.size())
                .averageTransaction(averageTransaction)
                .currency(currency)
                .build();
    }
    
    private Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> calculateCategoryBreakdown(
            List<Transaction> transactions, BigDecimal totalSpent) {
        
        Map<SpendingCategory, List<Transaction>> transactionsByCategory = transactions.stream()
                .filter(tx -> tx.getCategory() != null)
                .collect(Collectors.groupingBy(tx -> SpendingCategory.valueOf(tx.getCategory())));
        
        Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> breakdown = new EnumMap<>(SpendingCategory.class);
        
        transactionsByCategory.forEach((category, txList) -> {
            BigDecimal categoryTotal = txList.stream()
                    .filter(this::isExpense)
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Double percentage = totalSpent.compareTo(BigDecimal.ZERO) > 0 ?
                    categoryTotal.divide(totalSpent, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue() : 0.0;
            
            BigDecimal avgPerTransaction = txList.isEmpty() ? BigDecimal.ZERO :
                    categoryTotal.divide(BigDecimal.valueOf(txList.size()), 2, RoundingMode.HALF_UP);
            
            breakdown.put(category, FinancialAnalysisResponse.CategoryBreakdown.builder()
                    .category(category)
                    .amount(categoryTotal.setScale(2, RoundingMode.HALF_UP))
                    .percentage(Math.round(percentage * 10.0) / 10.0)
                    .transactionCount(txList.size())
                    .averagePerTransaction(avgPerTransaction)
                    .trend("STABLE") // Can be enhanced with historical data
                    .build());
        });
        
        return breakdown;
    }
    
    private FinancialAnalysisResponse.TrendAnalysis analyzeTrends(
            Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown) {
        
        Map<SpendingCategory, String> categoryTrends = new EnumMap<>(SpendingCategory.class);
        categoryBreakdown.forEach((category, breakdown) -> 
                categoryTrends.put(category, breakdown.getTrend()));
        
        List<String> patterns = new ArrayList<>();
        
        // Identify spending patterns
        categoryBreakdown.forEach((category, breakdown) -> {
            if (breakdown.getPercentage() > 40) {
                patterns.add(String.format("Heavy spending in %s category", category.getDisplayName()));
            }
            if (breakdown.getTransactionCount() > 20) {
                patterns.add(String.format("Frequent %s transactions", category.getDisplayName()));
            }
        });
        
        String overallTrend = determineOverallTrend(categoryBreakdown);
        
        return FinancialAnalysisResponse.TrendAnalysis.builder()
                .overallTrend(overallTrend)
                .categoryTrends(categoryTrends)
                .patterns(patterns)
                .build();
    }
    
    private String determineOverallTrend(
            Map<SpendingCategory, FinancialAnalysisResponse.CategoryBreakdown> categoryBreakdown) {
        
        long upCount = categoryBreakdown.values().stream()
                .filter(b -> "UP".equals(b.getTrend()))
                .count();
        
        long downCount = categoryBreakdown.values().stream()
                .filter(b -> "DOWN".equals(b.getTrend()))
                .count();
        
        if (upCount > downCount) {
            return "Spending is increasing across categories";
        } else if (downCount > upCount) {
            return "Spending is decreasing - good progress!";
        } else {
            return "Spending patterns are stable";
        }
    }
    
    private boolean isExpense(Transaction tx) {
        if (tx.getCategory() != null) {
            SpendingCategory category = SpendingCategory.valueOf(tx.getCategory());
            return category != SpendingCategory.INCOME && category != SpendingCategory.TRANSFER;
        }
        return "WITHDRAWAL".equals(tx.getType()) || "PURCHASE".equals(tx.getType());
    }
    
    private boolean isIncome(Transaction tx) {
        if (tx.getCategory() != null) {
            return SpendingCategory.INCOME.name().equals(tx.getCategory());
        }
        return "DEPOSIT".equals(tx.getType()) || "TOP_UP".equals(tx.getType());
    }
    
    private FinancialAnalysisResponse createEmptyAnalysis(UUID userId, String period) {
        return FinancialAnalysisResponse.builder()
                .userId(userId.toString())
                .period(period)
                .summary(FinancialAnalysisResponse.AnalysisSummary.builder()
                        .totalSpent(BigDecimal.ZERO)
                        .totalIncome(BigDecimal.ZERO)
                        .netSavings(BigDecimal.ZERO)
                        .transactionCount(0)
                        .averageTransaction(BigDecimal.ZERO)
                        .currency("EUR")
                        .build())
                .categoryBreakdown(new HashMap<>())
                .recommendations(List.of())
                .insights(List.of())
                .trends(FinancialAnalysisResponse.TrendAnalysis.builder()
                        .overallTrend("No data available")
                        .categoryTrends(new HashMap<>())
                        .patterns(List.of())
                        .build())
                .build();
    }
}
