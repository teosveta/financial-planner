package com.paysafe.hackcash.financialplanner.dto;

import com.paysafe.hackcash.financialplanner.model.SpendingCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAnalysisResponse {
    
    private String userId;
    private String period;
    private AnalysisSummary summary;
    private Map<SpendingCategory, CategoryBreakdown> categoryBreakdown;
    private List<AIRecommendation> recommendations;
    private List<SpendingInsight> insights;
    private TrendAnalysis trends;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalysisSummary {
        private BigDecimal totalSpent;
        private BigDecimal totalIncome;
        private BigDecimal netSavings;
        private Integer transactionCount;
        private BigDecimal averageTransaction;
        private String currency;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private SpendingCategory category;
        private BigDecimal amount;
        private Double percentage;
        private Integer transactionCount;
        private BigDecimal averagePerTransaction;
        private String trend; // UP, DOWN, STABLE
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AIRecommendation {
        private String type; // SAVINGS, BUDGET, ALERT, OPPORTUNITY
        private String title;
        private String description;
        private BigDecimal potentialSavings;
        private String priority; // HIGH, MEDIUM, LOW
        private SpendingCategory relatedCategory;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpendingInsight {
        private String message;
        private SpendingCategory category;
        private String sentiment; // POSITIVE, NEUTRAL, NEGATIVE
        private Map<String, Object> data;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendAnalysis {
        private String overallTrend;
        private Map<SpendingCategory, String> categoryTrends;
        private List<String> patterns;
    }
}
