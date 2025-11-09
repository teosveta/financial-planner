package com.financialplanner.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialplanner.dto.JsonImportDTO;
import com.financialplanner.dto.TransactionDTO;
import com.financialplanner.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JsonImportService {

    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;
    private final TransactionAnalysisService analysisService;

    /**
     * Import transactions from JSON file
     */
    @Transactional
    public JsonImportDTO.ImportResponse importFromFile(MultipartFile file) {
        log.info("Starting import from file: {}", file.getOriginalFilename());

        try {
            // Parse JSON file
            JsonImportDTO.ImportRequest importRequest = objectMapper.readValue(
                    file.getInputStream(),
                    JsonImportDTO.ImportRequest.class
            );

            return processImport(importRequest);

        } catch (IOException e) {
            log.error("Error reading JSON file: {}", e.getMessage(), e);
            return createErrorResponse("Failed to parse JSON file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing import: {}", e.getMessage(), e);
            return createErrorResponse("Import failed: " + e.getMessage());
        }
    }

    /**
     * Import transactions from JSON string (for API integration with Digital Wallet)
     */
    @Transactional
    public JsonImportDTO.ImportResponse importFromJson(String jsonData) {
        log.info("Starting import from JSON string");

        try {
            JsonImportDTO.ImportRequest importRequest = objectMapper.readValue(
                    jsonData,
                    JsonImportDTO.ImportRequest.class
            );

            return processImport(importRequest);

        } catch (IOException e) {
            log.error("Error parsing JSON: {}", e.getMessage(), e);
            return createErrorResponse("Failed to parse JSON: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing import: {}", e.getMessage(), e);
            return createErrorResponse("Import failed: " + e.getMessage());
        }
    }

    /**
     * Import transactions from request object (for REST API)
     */
    @Transactional
    public JsonImportDTO.ImportResponse importFromRequest(JsonImportDTO.ImportRequest request) {
        log.info("Starting import from request object for user: {}", request.getUser().getUserId());
        return processImport(request);
    }

    /**
     * Process the import request
     */
    private JsonImportDTO.ImportResponse processImport(JsonImportDTO.ImportRequest request) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        int duplicateCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        log.info("Processing {} transactions for user: {}",
                request.getTransactions().size(),
                request.getUser().getUserId());

        // Process each transaction
        for (JsonImportDTO.TransactionImport txImport : request.getTransactions()) {
            try {
                // Check for duplicates (based on external transaction ID)
                if (txImport.getTransactionId() != null && isDuplicate(txImport.getTransactionId())) {
                    duplicateCount++;
                    log.debug("Skipping duplicate transaction: {}", txImport.getTransactionId());
                    continue;
                }

                // Convert and save transaction
                TransactionDTO.Request txRequest = convertToTransactionRequest(txImport);
                TransactionDTO.Response saved = transactionService.createTransaction(txRequest);

                successCount++;
                totalAmount = totalAmount.add(txImport.getAmount());

                log.debug("Successfully imported transaction: {} - ${} at {}",
                        txImport.getMerchantName(),
                        txImport.getAmount(),
                        saved.getCategory());

            } catch (Exception e) {
                failCount++;
                String error = String.format("Failed to import transaction '%s': %s",
                        txImport.getDescription(), e.getMessage());
                errors.add(error);
                log.warn(error);
            }
        }

        // Create import summary
        JsonImportDTO.ImportSummary summary = JsonImportDTO.ImportSummary.builder()
                .totalTransactions(request.getTransactions().size())
                .successfulImports(successCount)
                .failedImports(failCount)
                .duplicates(duplicateCount)
                .totalAmount(totalAmount)
                .importTimestamp(LocalDateTime.now())
                .build();

        // Generate analysis if we have successful imports
        JsonImportDTO.AnalysisResult analysis = null;
        if (successCount > 0) {
            try {
                analysis = generateAnalysis();
            } catch (Exception e) {
                log.warn("Failed to generate analysis: {}", e.getMessage());
            }
        }

        // Build response
        return JsonImportDTO.ImportResponse.builder()
                .success(failCount == 0)
                .message(buildSuccessMessage(successCount, failCount, duplicateCount))
                .summary(summary)
                .errors(errors.isEmpty() ? null : errors)
                .analysis(analysis)
                .build();
    }

    /**
     * Convert import transaction to internal format
     */
    private TransactionDTO.Request convertToTransactionRequest(
            JsonImportDTO.TransactionImport txImport) {

        return TransactionDTO.Request.builder()
                .description(txImport.getDescription())
                .merchantName(txImport.getMerchantName())
                .amount(txImport.getAmount())
                .transactionDate(txImport.getTransactionDate() != null
                        ? txImport.getTransactionDate()
                        : LocalDateTime.now())
                .build();
    }

    /**
     * Check if transaction already exists (simple duplicate detection)
     */
    private boolean isDuplicate(String externalId) {
        // In a real implementation, you'd check against a stored mapping
        // For now, we'll just return false to allow all imports
        // TODO: Implement proper duplicate detection with external ID mapping
        return false;
    }

    /**
     * Generate analysis after successful import
     */
    private JsonImportDTO.AnalysisResult generateAnalysis() {
        TransactionDTO.AnalysisReport report = analysisService.analyzeTransactions("monthly");

        return JsonImportDTO.AnalysisResult.builder()
                .totalExpenses(report.getTotalExpenses())
                .categoryBreakdown(report.getCategoryBreakdown())
                .aiRecommendations(report.getAiRecommendations())
                .period("monthly")
                .build();
    }

    /**
     * Build success message
     */
    private String buildSuccessMessage(int success, int failed, int duplicates) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Import completed: %d successful", success));

        if (duplicates > 0) {
            message.append(String.format(", %d duplicates skipped", duplicates));
        }

        if (failed > 0) {
            message.append(String.format(", %d failed", failed));
        }

        return message.toString();
    }

    /**
     * Create error response
     */
    private JsonImportDTO.ImportResponse createErrorResponse(String message) {
        return JsonImportDTO.ImportResponse.builder()
                .success(false)
                .message(message)
                .summary(JsonImportDTO.ImportSummary.builder()
                        .totalTransactions(0)
                        .successfulImports(0)
                        .failedImports(0)
                        .duplicates(0)
                        .totalAmount(BigDecimal.ZERO)
                        .importTimestamp(LocalDateTime.now())
                        .build())
                .errors(List.of(message))
                .build();
    }

    /**
     * Validate import request
     */
    public List<String> validateImportRequest(JsonImportDTO.ImportRequest request) {
        List<String> validationErrors = new ArrayList<>();

        if (request.getUser() == null) {
            validationErrors.add("User information is required");
            return validationErrors;
        }

        if (request.getUser().getUserId() == null || request.getUser().getUserId().isBlank()) {
            validationErrors.add("User ID is required");
        }

        if (request.getTransactions() == null || request.getTransactions().isEmpty()) {
            validationErrors.add("At least one transaction is required");
            return validationErrors;
        }

        // Validate each transaction
        for (int i = 0; i < request.getTransactions().size(); i++) {
            JsonImportDTO.TransactionImport tx = request.getTransactions().get(i);
            String prefix = "Transaction " + (i + 1) + ": ";

            if (tx.getMerchantName() == null || tx.getMerchantName().isBlank()) {
                validationErrors.add(prefix + "Merchant name is required");
            }

            if (tx.getDescription() == null || tx.getDescription().isBlank()) {
                validationErrors.add(prefix + "Description is required");
            }

            if (tx.getAmount() == null || tx.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                validationErrors.add(prefix + "Amount must be greater than zero");
            }
        }

        return validationErrors;
    }
}
