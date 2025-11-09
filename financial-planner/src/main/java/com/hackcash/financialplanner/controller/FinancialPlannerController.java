package com.hackcash.financialplanner.controller;

import com.hackcash.financialplanner.dto.RecommendationDTO;
import com.hackcash.financialplanner.dto.SpendingAnalysisDTO;
import com.hackcash.financialplanner.dto.TransactionDTO;
import com.hackcash.financialplanner.model.Transaction;
import com.hackcash.financialplanner.service.AIRecommendationEngine;
import com.hackcash.financialplanner.service.TransactionAnalysisService;
import com.hackcash.financialplanner.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Financial Planner REST API Controller
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Enable CORS for frontend
public class FinancialPlannerController {
    
    private final TransactionService transactionService;
    private final TransactionAnalysisService analysisService;
    private final AIRecommendationEngine recommendationEngine;
    
    /**
     * Health check endpoint (for Kubernetes)
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "financial-planner");
        health.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
    
    /**
     * Create new transaction
     */
    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionDTO dto) {
        log.info("Creating transaction: {}", dto.getMerchantName());
        Transaction transaction = transactionService.createTransaction(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }
    
    /**
     * Get all transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get recent transactions
     */
    @GetMapping("/transactions/recent")
    public ResponseEntity<List<Transaction>> getRecentTransactions() {
        List<Transaction> transactions = transactionService.getRecentTransactions();
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Get transaction by ID
     */
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable UUID id) {
        Transaction transaction = transactionService.getTransactionById(id);
        return ResponseEntity.ok(transaction);
    }
    
    /**
     * Delete transaction
     */
    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        log.info("Deleting transaction: {}", id);
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get spending analysis for a period
     * @param period Number of time units (default: 1)
     * @param unit Time unit: day, week, month, year (default: month)
     */
    @GetMapping("/analysis")
    public ResponseEntity<SpendingAnalysisDTO> getSpendingAnalysis(
            @RequestParam(defaultValue = "1") int period,
            @RequestParam(defaultValue = "month") String unit) {
        
        log.info("Analyzing spending for {} {}", period, unit);
        SpendingAnalysisDTO analysis = analysisService.analyzeSpending(period, unit);
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * Get AI-powered recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDTO>> getRecommendations(
            @RequestParam(defaultValue = "1") int period,
            @RequestParam(defaultValue = "month") String unit) {
        
        log.info("Generating recommendations for {} {}", period, unit);
        
        // Get spending analysis
        SpendingAnalysisDTO analysis = analysisService.analyzeSpending(period, unit);
        
        // Generate AI recommendations
        List<RecommendationDTO> recommendations = recommendationEngine.generateRecommendations(analysis);
        
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * Get complete dashboard data (analysis + recommendations)
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(defaultValue = "1") int period,
            @RequestParam(defaultValue = "month") String unit) {
        
        log.info("Loading dashboard data for {} {}", period, unit);
        
        SpendingAnalysisDTO analysis = analysisService.analyzeSpending(period, unit);
        List<RecommendationDTO> recommendations = recommendationEngine.generateRecommendations(analysis);
        List<Transaction> recentTransactions = transactionService.getRecentTransactions();
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("analysis", analysis);
        dashboard.put("recommendations", recommendations);
        dashboard.put("recentTransactions", recentTransactions);
        dashboard.put("period", period);
        dashboard.put("unit", unit);
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Get spending trend over multiple periods
     */
    @GetMapping("/trends")
    public ResponseEntity<List<Map<String, Object>>> getSpendingTrend(
            @RequestParam(defaultValue = "6") int months) {
        
        log.info("Loading spending trend for {} months", months);
        List<Map<String, Object>> trend = analysisService.getSpendingTrend(months);
        return ResponseEntity.ok(trend);
    }
    
    /**
     * Trigger manual JSON import (for testing)
     */
    @PostMapping("/import/trigger")
    public ResponseEntity<Map<String, String>> triggerImport() {
        log.info("Manually triggering JSON import");
        
        try {
            transactionService.importTransactionsFromJSON();
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Import completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Import failed: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
