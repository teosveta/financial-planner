package com.hackcash.financialplanner.service;

import com.hackcash.financialplanner.dto.CategoryAnalysisDTO;
import com.hackcash.financialplanner.dto.SpendingAnalysisDTO;
import com.hackcash.financialplanner.model.Transaction;
import com.hackcash.financialplanner.model.TransactionCategory;
import com.hackcash.financialplanner.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Transaction Analysis Service
 * Calculates spending patterns, trends, and category breakdowns
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionAnalysisService {
    
    private final TransactionRepository transactionRepository;
    
    /**
     * Analyze spending for a given period (Monthly or Weekly)
     */
    @Cacheable(value = "category-analysis", key = "#period + '-' + #unit")
    public SpendingAnalysisDTO analyzeSpending(int period, String unit) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(endDate, period, unit);
        
        log.info("Analyzing spending from {} to {}", startDate, endDate);
        
        // Get all transactions in period
        List<Transaction> transactions = transactionRepository
            .findByTransactionDateBetween(startDate, endDate);
        
        // Calculate total expenses (exclude INCOME and SAVINGS)
        BigDecimal totalExpenses = transactions.stream()
            .filter(t -> t.getCategory() != TransactionCategory.INCOME && 
                        t.getCategory() != TransactionCategory.SAVINGS)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate total income
        BigDecimal totalIncome = transactions.stream()
            .filter(t -> t.getCategory() == TransactionCategory.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Group by category and calculate percentages
        Map<TransactionCategory, List<Transaction>> byCategory = transactions.stream()
            .filter(t -> t.getCategory() != TransactionCategory.INCOME && 
                        t.getCategory() != TransactionCategory.SAVINGS)
            .collect(Collectors.groupingBy(Transaction::getCategory));
        
        List<CategoryAnalysisDTO> categoryBreakdown = new ArrayList<>();
        
        for (Map.Entry<TransactionCategory, List<Transaction>> entry : byCategory.entrySet()) {
            TransactionCategory category = entry.getKey();
            List<Transaction> categoryTransactions = entry.getValue();
            
            BigDecimal categoryTotal = categoryTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                ? categoryTotal.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue()
                : 0.0;
            
            CategoryAnalysisDTO categoryAnalysis = CategoryAnalysisDTO.builder()
                .category(category.name())
                .categoryDisplayName(category.getDisplayName())
                .categoryIcon(category.getIcon())
                .totalAmount(categoryTotal)
                .transactionCount(categoryTransactions.size())
                .percentage(percentage)
                .averageTransactionAmount(
                    categoryTotal.divide(
                        BigDecimal.valueOf(categoryTransactions.size()), 
                        2, 
                        RoundingMode.HALF_UP
                    )
                )
                .recommendedPercentage(category.getRecommendedPercentage())
                .build();
            
            categoryBreakdown.add(categoryAnalysis);
        }
        
        // Sort by total amount descending
        categoryBreakdown.sort((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()));
        
        // Calculate previous period for comparison
        LocalDateTime previousEndDate = startDate;
        LocalDateTime previousStartDate = calculateStartDate(previousEndDate, period, unit);
        
        BigDecimal previousPeriodExpenses = transactionRepository
            .getTotalExpenses(previousStartDate, previousEndDate);
        
        if (previousPeriodExpenses == null) {
            previousPeriodExpenses = BigDecimal.ZERO;
        }
        
        // Calculate trend
        double trendPercentage = 0.0;
        if (previousPeriodExpenses.compareTo(BigDecimal.ZERO) > 0) {
            trendPercentage = totalExpenses.subtract(previousPeriodExpenses)
                .divide(previousPeriodExpenses, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }
        
        // Calculate savings rate
        double savingsRate = 0.0;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savedAmount = totalIncome.subtract(totalExpenses);
            savingsRate = savedAmount
                .divide(totalIncome, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
        }
        
        return SpendingAnalysisDTO.builder()
            .periodStart(startDate)
            .periodEnd(endDate)
            .totalExpenses(totalExpenses)
            .totalIncome(totalIncome)
            .categoryBreakdown(categoryBreakdown)
            .transactionCount(transactions.size())
            .previousPeriodExpenses(previousPeriodExpenses)
            .trendPercentage(trendPercentage)
            .savingsRate(savingsRate)
            .topSpendingCategory(categoryBreakdown.isEmpty() ? null : categoryBreakdown.get(0))
            .build();
    }
    
    /**
     * Get spending trend over time
     */
    public List<Map<String, Object>> getSpendingTrend(int months) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime endDate = LocalDateTime.now();
        
        for (int i = 0; i < months; i++) {
            LocalDateTime monthEnd = endDate.minusMonths(i);
            LocalDateTime monthStart = monthEnd.minusMonths(1);
            
            BigDecimal monthlyExpenses = transactionRepository
                .getTotalExpenses(monthStart, monthEnd);
            
            if (monthlyExpenses == null) {
                monthlyExpenses = BigDecimal.ZERO;
            }
            
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", monthStart.getMonth().toString());
            monthData.put("year", monthStart.getYear());
            monthData.put("totalExpenses", monthlyExpenses);
            
            trend.add(0, monthData); // Add at beginning to maintain chronological order
        }
        
        return trend;
    }
    
    /**
     * Calculate start date based on period and unit
     */
    private LocalDateTime calculateStartDate(LocalDateTime endDate, int period, String unit) {
        return switch (unit.toLowerCase()) {
            case "day" -> endDate.minus(period, ChronoUnit.DAYS);
            case "week" -> endDate.minus(period, ChronoUnit.WEEKS);
            case "month" -> endDate.minus(period, ChronoUnit.MONTHS);
            case "year" -> endDate.minus(period, ChronoUnit.YEARS);
            default -> endDate.minus(1, ChronoUnit.MONTHS); // Default to 1 month
        };
    }
    
    /**
     * Get category comparison across multiple periods
     */
    public Map<String, List<BigDecimal>> getCategoryTrends(TransactionCategory category, int periods) {
        Map<String, List<BigDecimal>> trends = new HashMap<>();
        LocalDateTime endDate = LocalDateTime.now();
        
        List<BigDecimal> amounts = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < periods; i++) {
            LocalDateTime periodEnd = endDate.minusMonths(i);
            LocalDateTime periodStart = periodEnd.minusMonths(1);
            
            List<Transaction> periodTransactions = transactionRepository
                .findByCategoryAndTransactionDateBetween(category, periodStart, periodEnd);
            
            BigDecimal periodTotal = periodTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            amounts.add(0, periodTotal);
            labels.add(0, periodStart.getMonth().toString().substring(0, 3));
        }
        
        trends.put("amounts", amounts);
        trends.put("labels", labels.stream().map(l -> BigDecimal.valueOf(l.hashCode())).collect(Collectors.toList())); // Placeholder
        
        return trends;
    }
}
