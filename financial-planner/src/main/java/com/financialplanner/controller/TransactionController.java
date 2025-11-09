package com.financialplanner.controller;

import com.financialplanner.dto.TransactionDTO;
import com.financialplanner.model.Transaction.TransactionCategory;
import com.financialplanner.service.ClaudeAIService;
import com.financialplanner.service.TransactionAnalysisService;
import com.financialplanner.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // For development - restrict in production
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionAnalysisService analysisService;
    private final ClaudeAIService claudeAIService;

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
     * Check AI service status
     * GET /api/v1/transactions/ai/status
     */
    @GetMapping("/ai/status")
    public ResponseEntity<Map<String, Object>> getAIStatus() {
        Map<String, Object> status = new HashMap<>();
        boolean available = claudeAIService.isAvailable();
        
        status.put("available", available);
        status.put("service", "Claude AI (Anthropic)");
        status.put("provider", "Anthropic");
        status.put("enabled", true);
        
        if (available) {
            List<String> models = claudeAIService.getAvailableModels();
            status.put("models", models);
            status.put("message", "✅ Claude AI is configured and ready! Real AI-powered financial insights active.");
            status.put("modelCount", models.size());
            status.put("features", List.of(
                "Smart transaction categorization",
                "Personalized spending recommendations",
                "Anomaly detection",
                "Budget insights"
            ));
        } else {
            status.put("models", List.of());
            status.put("message", "❌ Claude AI is not configured. Add your API key to application.properties");
            status.put("setupGuide", "Get your free API key at: https://console.anthropic.com/");
        }
        
        return ResponseEntity.ok(status);
    }

    /**
     * Test AI with a simple prompt
     * POST /api/v1/transactions/ai/test
     * Body: { "prompt": "Your test question" }
     */
    @PostMapping("/ai/test")
    public ResponseEntity<Map<String, Object>> testAI(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String prompt = request.getOrDefault("prompt", 
            "You are a financial advisor. Give one quick money-saving tip in under 50 words.");
        
        log.info("Testing Claude AI with prompt: {}", prompt);
        
        boolean available = claudeAIService.isAvailable();
        if (!available) {
            response.put("success", false);
            response.put("error", "Claude AI is not configured");
            response.put("message", "Please add your Claude API key to application.properties");
            response.put("setupGuide", "Get your API key at: https://console.anthropic.com/");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
        
        try {
            long startTime = System.currentTimeMillis();
            String aiResponse = claudeAIService.generateRecommendations(prompt);
            long duration = System.currentTimeMillis() - startTime;
            
            response.put("success", true);
            response.put("prompt", prompt);
            response.put("response", aiResponse);
            response.put("duration_ms", duration);
            response.put("model", "claude-3-5-sonnet-20241022");
            response.put("provider", "Anthropic Claude");
            response.put("message", "🎉 Claude AI is working! This is a REAL AI-generated response from Claude.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing Claude AI: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("troubleshooting", "Check your API key and internet connection");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get smart spending insights
     * GET /api/v1/transactions/ai/insights?period=monthly
     */
    @GetMapping("/ai/insights")
    public ResponseEntity<Map<String, Object>> getSpendingInsights(
            @RequestParam(defaultValue = "monthly") String period) {
        Map<String, Object> insights = new HashMap<>();
        
        try {
            TransactionDTO.AnalysisReport report = analysisService.analyzeTransactions(period);
            
            // Prepare category spending map for AI
            Map<String, BigDecimal> categorySpending = new HashMap<>();
            for (TransactionDTO.CategoryStats stats : report.getCategoryBreakdown()) {
                categorySpending.put(stats.getCategoryDisplayName(), stats.getTotalAmount());
            }
            
            // Get AI-powered insight
            String aiInsight = claudeAIService.generateSpendingInsight(
                period, 
                categorySpending, 
                report.getTotalExpenses()
            );
            
            insights.put("success", true);
            insights.put("period", period);
            insights.put("insight", aiInsight);
            insights.put("totalExpenses", report.getTotalExpenses());
            insights.put("aiPowered", claudeAIService.isAvailable());
            
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            log.error("Error generating insights: {}", e.getMessage());
            insights.put("success", false);
            insights.put("error", "Failed to generate insights");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(insights);
        }
    }

    /**
     * Predict category for a transaction before saving
     * POST /api/v1/transactions/ai/predict-category
     * Body: { "merchantName": "Starbucks", "description": "Coffee", "amount": 5.99 }
     */
    @PostMapping("/ai/predict-category")
    public ResponseEntity<Map<String, Object>> predictCategory(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        String merchantName = (String) request.get("merchantName");
        String description = (String) request.getOrDefault("description", "");
        Double amount = request.containsKey("amount") ? 
            Double.parseDouble(request.get("amount").toString()) : 0.0;
        
        boolean aiAvailable = claudeAIService.isAvailable();
        String predictedCategory = null;
        
        if (aiAvailable) {
            try {
                predictedCategory = claudeAIService.categorizeTransaction(
                    merchantName, 
                    description, 
                    BigDecimal.valueOf(amount)
                );
            } catch (Exception e) {
                log.error("AI categorization failed: {}", e.getMessage());
            }
        }
        
        response.put("merchantName", merchantName);
        response.put("predictedCategory", predictedCategory);
        response.put("aiPowered", predictedCategory != null);
        response.put("confidence", predictedCategory != null ? "high" : "rule-based");
        response.put("message", predictedCategory != null ? 
            "Category predicted by Claude AI" : 
            "AI not available, will use rule-based categorization");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get personalized financial tips
     * GET /api/v1/transactions/ai/tips
     */
    @GetMapping("/ai/tips")
    public ResponseEntity<Map<String, Object>> getFinancialTips() {
        Map<String, Object> response = new HashMap<>();
        
        if (!claudeAIService.isAvailable()) {
            response.put("success", false);
            response.put("message", "Claude AI not configured");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
        
        try {
            String prompt = "As a financial advisor AI, provide 3 quick, actionable money management tips " +
                          "for someone looking to improve their financial health. Keep each tip under 100 words. " +
                          "Format as numbered list with emojis.";
            
            String tips = claudeAIService.generateRecommendations(prompt);
            
            response.put("success", true);
            response.put("tips", tips);
            response.put("provider", "Claude AI (Anthropic)");
            response.put("timestamp", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating tips: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint
     * GET /api/v1/transactions/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Financial Planner API with Claude AI");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("ai_provider", "Claude by Anthropic");
        health.put("ai_enabled", String.valueOf(claudeAIService.isAvailable()));
        health.put("ai_model", "claude-3-5-sonnet-20241022");
        return ResponseEntity.ok(health);
    }
}
