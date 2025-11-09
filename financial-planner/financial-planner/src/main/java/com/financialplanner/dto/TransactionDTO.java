package com.financialplanner.dto;

import com.financialplanner.model.Transaction.TransactionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Description is required")
        private String description;

        @NotBlank(message = "Merchant name is required")
        private String merchantName;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        private BigDecimal amount;

        private LocalDateTime transactionDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String description;
        private String merchantName;
        private BigDecimal amount;
        private TransactionCategory category;
        private String categoryDisplayName;
        private LocalDateTime transactionDate;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryStats {
        private TransactionCategory category;
        private String categoryDisplayName;
        private BigDecimal totalAmount;
        private Integer transactionCount;
        private Double percentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalysisReport {
        private String period;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private BigDecimal totalExpenses;
        private List<CategoryStats> categoryBreakdown;
        private List<String> aiRecommendations;
    }
}
