package com.financialplanner.controller;

import com.financialplanner.dto.TransactionDTO;
import com.financialplanner.model.Transaction.TransactionCategory;
import com.financialplanner.service.TransactionAnalysisService;
import com.financialplanner.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // For development - restrict in production
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionAnalysisService analysisService;

    /**
     * Create a new transaction
     * POST /api/v1/transactions
     */
    @PostMapping
    public ResponseEntity<TransactionDTO.Response> createTransaction(
            @Valid @RequestBody TransactionDTO.Request request) {
        log.info("Received request to create transaction: {}", request.getMerchantName());
        TransactionDTO.Response response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all transactions
     * GET /api/v1/transactions
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO.Response>> getAllTransactions() {
        List<TransactionDTO.Response> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transaction by ID
     * GET /api/v1/transactions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO.Response> getTransactionById(@PathVariable Long id) {
        TransactionDTO.Response transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }

    /**
     * Get transactions by category
     * GET /api/v1/transactions/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<TransactionDTO.Response>> getTransactionsByCategory(
            @PathVariable TransactionCategory category) {
        List<TransactionDTO.Response> transactions = 
                transactionService.getTransactionsByCategory(category);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Delete transaction
     * DELETE /api/v1/transactions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.info("Received request to delete transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get analysis report for a specific period
     * GET /api/v1/transactions/analysis?period=monthly
     */
    @GetMapping("/analysis")
    public ResponseEntity<TransactionDTO.AnalysisReport> getAnalysis(
            @RequestParam(defaultValue = "monthly") String period) {
        log.info("Received request for analysis report: {}", period);
        TransactionDTO.AnalysisReport report = analysisService.analyzeTransactions(period);
        return ResponseEntity.ok(report);
    }

    /**
     * Get statistics for specific category
     * GET /api/v1/transactions/stats/{category}?period=monthly
     */
    @GetMapping("/stats/{category}")
    public ResponseEntity<TransactionDTO.CategoryStats> getCategoryStatistics(
            @PathVariable TransactionCategory category,
            @RequestParam(defaultValue = "monthly") String period) {
        TransactionDTO.CategoryStats stats = 
                analysisService.getCategoryStatistics(category, period);
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint
     * GET /api/v1/transactions/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Financial Planner API is running!");
    }
}
