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
        LocalDateTime[] currentRange = calculateDateRange(period);
        LocalDateTime startDate = currentRange[0];
        LocalDateTime endDate = currentRange[1];
        LocalDateTime[] previousRange = calculatePreviousDateRange(period, startDate, endDate);

        log.info("Analyzing transactions for period: {} ({} to {})", period, startDate, endDate);

        // Get all transactions in period
        List<Transaction> transactions = transactionRepository.findTransactionsInPeriod(startDate, endDate);

        // Calculate total expenses
        BigDecimal totalExpenses = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate category breakdown
        List<TransactionDTO.CategoryStats> categoryBreakdown = calculateCategoryStats(
                transactions, totalExpenses, previousRange
        );

        BigDecimal previousTotalExpenses = transactionRepository.sumTotalInPeriod(previousRange[0], previousRange[1]);
        if (previousTotalExpenses == null) {
            previousTotalExpenses = BigDecimal.ZERO;
        }

        // Generate AI recommendations
        List<String> recommendations = aiRecommendationEngine.generateRecommendations(
                totalExpenses, previousTotalExpenses, categoryBreakdown
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
            BigDecimal totalExpenses,
            LocalDateTime[] previousRange) {

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

                    BigDecimal previousTotal = transactionRepository.sumAmountByCategoryInPeriod(
                            category, previousRange[0], previousRange[1]);
                    if (previousTotal == null) {
                        previousTotal = BigDecimal.ZERO;
                    }

                    double percentage = categoryTotal
                            .divide(totalExpenses, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();

                    double percentageChange = 0.0;
                    if (previousTotal.compareTo(BigDecimal.ZERO) > 0) {
                        percentageChange = categoryTotal.subtract(previousTotal)
                                .divide(previousTotal, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .doubleValue();
                    } else if (categoryTotal.compareTo(BigDecimal.ZERO) > 0) {
                        percentageChange = 100.0;
                    }

                    return TransactionDTO.CategoryStats.builder()
                            .category(category)
                            .categoryDisplayName(category.getDisplayName())
                            .totalAmount(categoryTotal)
                            .transactionCount(categoryTransactions.size())
                            .percentage(percentage)
                            .previousPeriodTotal(previousTotal)
                            .percentageChange(percentageChange)
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

    private LocalDateTime[] calculatePreviousDateRange(String period, LocalDateTime currentStart, LocalDateTime currentEnd) {
        LocalDateTime previousStart;
        LocalDateTime previousEnd;

        switch (period.toLowerCase()) {
            case "weekly":
                previousEnd = currentStart;
                previousStart = currentStart.minusWeeks(1);
                break;
            case "monthly":
                YearMonth currentMonth = YearMonth.from(currentStart);
                YearMonth previousMonth = currentMonth.minusMonths(1);
                previousStart = previousMonth.atDay(1).atStartOfDay();
                previousEnd = previousMonth.atEndOfMonth().atTime(23, 59, 59);
                break;
            case "yearly":
                previousEnd = currentStart;
                previousStart = currentStart.minusYears(1);
                break;
            default:
                YearMonth month = YearMonth.from(currentStart);
                YearMonth prior = month.minusMonths(1);
                previousStart = prior.atDay(1).atStartOfDay();
                previousEnd = prior.atEndOfMonth().atTime(23, 59, 59);
        }

        return new LocalDateTime[]{previousStart, previousEnd};
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

        LocalDateTime[] previousRange = calculatePreviousDateRange(period, dateRange[0], dateRange[1]);

        List<Transaction> categoryTransactions = transactions.stream()
                .filter(t -> t.getCategory() == category)
                .toList();

        BigDecimal categoryTotal = categoryTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal previousTotal = transactionRepository.sumAmountByCategoryInPeriod(
                category, previousRange[0], previousRange[1]);
        if (previousTotal == null) {
            previousTotal = BigDecimal.ZERO;
        }

        BigDecimal totalExpenses = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal previousTotalExpenses = transactionRepository.sumTotalInPeriod(previousRange[0], previousRange[1]);
        if (previousTotalExpenses == null) {
            previousTotalExpenses = BigDecimal.ZERO;
        }

        double percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                ? categoryTotal.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
                : 0.0;

        double percentageChange = 0.0;
        if (previousTotal.compareTo(BigDecimal.ZERO) > 0) {
            percentageChange = categoryTotal.subtract(previousTotal)
                    .divide(previousTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else if (categoryTotal.compareTo(BigDecimal.ZERO) > 0) {
            percentageChange = 100.0;
        }

        return TransactionDTO.CategoryStats.builder()
                .category(category)
                .categoryDisplayName(category.getDisplayName())
                .totalAmount(categoryTotal)
                .transactionCount(categoryTransactions.size())
                .percentage(percentage)
                .previousPeriodTotal(previousTotal)
                .percentageChange(percentageChange)
                .build();
    }
}
