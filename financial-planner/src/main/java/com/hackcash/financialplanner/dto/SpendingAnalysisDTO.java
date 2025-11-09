package com.hackcash.financialplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingAnalysisDTO {
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalExpenses;
    private BigDecimal totalIncome;
    private Integer transactionCount;
    private List<CategoryAnalysisDTO> categoryBreakdown;
    private BigDecimal previousPeriodExpenses;
    private Double trendPercentage;
    private Double savingsRate;
    private CategoryAnalysisDTO topSpendingCategory;
}
