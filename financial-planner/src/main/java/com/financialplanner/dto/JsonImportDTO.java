package com.financialplanner.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for importing user transaction data from JSON files
 * Can be used to integrate with Digital Wallet or import bulk data
 */
public class JsonImportDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportRequest {
        @NotNull(message = "User information is required")
        @Valid
        private UserInfo user;

        @NotNull(message = "Transactions list is required")
        @Size(min = 1, message = "At least one transaction is required")
        @Valid
        private List<TransactionImport> transactions;

        private ImportMetadata metadata;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        @NotBlank(message = "User ID is required")
        private String userId;

        private String username;
        private String email;
        private String walletId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionImport {
        private String transactionId; // External ID from wallet system

        @NotBlank(message = "Description is required")
        private String description;

        @NotBlank(message = "Merchant name is required")
        private String merchantName;

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        private BigDecimal amount;

        private LocalDateTime transactionDate;

        // Optional: pre-categorized from wallet system
        private String category;

        // Wallet-specific fields
        private String walletId;
        private String walletName;
        private String transactionType; // DEBIT, CREDIT, TRANSFER
        private String recipientName;
        private String recipientId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportMetadata {
        private String source; // "digital_wallet", "manual_upload", "api"
        private String importDate;
        private String version;
        private String periodStart;
        private String periodEnd;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportResponse {
        private boolean success;
        private String message;
        private ImportSummary summary;
        private List<String> errors;
        private AnalysisResult analysis;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportSummary {
        private int totalTransactions;
        private int successfulImports;
        private int failedImports;
        private int duplicates;
        private BigDecimal totalAmount;
        private LocalDateTime importTimestamp;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalysisResult {
        private BigDecimal totalExpenses;
        private List<TransactionDTO.CategoryStats> categoryBreakdown;
        private List<String> aiRecommendations;
        private String period;
    }
}
