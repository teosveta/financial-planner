package com.financialplanner.service;

import com.financialplanner.dto.TransactionDTO;
import com.financialplanner.model.Transaction;
import com.financialplanner.model.Transaction.TransactionCategory;
import com.financialplanner.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalysisService {

    private final TransactionRepository transactionRepository;
    private final AIRecommendationEngine aiRecommendationEngine;

    /**
     * Analyze transactions for a specific period (monthly, weekly)
     */
    @Transactional(readOnly = true)
    public TransactionDTO.AnalysisReport analyzeTransactions(String period) {
        LocalDateTime[] dateRange = calculateDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];

        log.info("Analyzing transactions for period: {} ({} to {})", period, startDate, endDate);

        // Get all transactions in period
        List<Transaction> transactions = transactionRepository.findTransactionsInPeriod(startDate, endDate);

        // Calculate total expenses
        BigDecimal totalExpenses = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate category breakdown
        List<TransactionDTO.CategoryStats> categoryBreakdown = calculateCategoryStats(
                transactions, totalExpenses
        );

        // Generate AI recommendations
        List<String> recommendations = aiRecommendationEngine.generateRecommendations(
                totalExpenses, categoryBreakdown
        );

        return TransactionDTO.AnalysisReport.builder()
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .totalExpenses(totalExpenses)
                .categoryBreakdown(categoryBreakdown)
                .aiRecommendations(recommendations)
                .build();
    }

    /**
     * Calculate statistics for each spending category
     */
    private List<TransactionDTO.CategoryStats> calculateCategoryStats(
            List<Transaction> transactions,
            BigDecimal totalExpenses) {

        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return new ArrayList<>();
        }

        return Arrays.stream(TransactionCategory.values())
                .map(category -> {
                    List<Transaction> categoryTransactions = transactions.stream()
                            .filter(t -> t.getCategory() == category)
                            .toList();

                    BigDecimal categoryTotal = categoryTransactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    double percentage = categoryTotal
                            .divide(totalExpenses, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();

                    return TransactionDTO.CategoryStats.builder()
                            .category(category)
                            .categoryDisplayName(category.getDisplayName())
                            .totalAmount(categoryTotal)
                            .transactionCount(categoryTransactions.size())
                            .percentage(percentage)
                            .build();
                })
                .filter(stats -> stats.getTotalAmount().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getTotalAmount().compareTo(a.getTotalAmount()))
                .collect(Collectors.toList());
    }

    /**
     * Calculate date range based on period type
     */
    private LocalDateTime[] calculateDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end = now;

        switch (period.toLowerCase()) {
            case "weekly":
                start = now.minusWeeks(1);
                break;
            case "monthly":
                YearMonth currentMonth = YearMonth.now();
                start = currentMonth.atDay(1).atStartOfDay();
                end = currentMonth.atEndOfMonth().atTime(23, 59, 59);
                break;
            case "yearly":
                start = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
                break;
            default:
                // Default to current month
                YearMonth month = YearMonth.now();
                start = month.atDay(1).atStartOfDay();
                end = month.atEndOfMonth().atTime(23, 59, 59);
        }

        return new LocalDateTime[]{start, end};
    }

    /**
     * Get statistics for a specific category
     */
    @Transactional(readOnly = true)
    public TransactionDTO.CategoryStats getCategoryStatistics(
            TransactionCategory category,
            String period) {

        LocalDateTime[] dateRange = calculateDateRange(period);
        List<Transaction> transactions = transactionRepository.findTransactionsInPeriod(
                dateRange[0], dateRange[1]
        );

        List<Transaction> categoryTransactions = transactions.stream()
                .filter(t -> t.getCategory() == category)
                .toList();

        BigDecimal categoryTotal = categoryTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                ? categoryTotal.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        return TransactionDTO.CategoryStats.builder()
                .category(category)
                .categoryDisplayName(category.getDisplayName())
                .totalAmount(categoryTotal)
                .transactionCount(categoryTransactions.size())
                .percentage(percentage)
                .build();
    }
}
