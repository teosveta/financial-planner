package com.paysafe.hackcash.financialplanner.controller;

import com.paysafe.hackcash.financialplanner.dto.FinancialAnalysisResponse;
import com.paysafe.hackcash.financialplanner.service.FinancialAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/financial-planner")
public class FinancialPlannerController {
    
    private final FinancialAnalysisService financialAnalysisService;
    
    public FinancialPlannerController(FinancialAnalysisService financialAnalysisService) {
        this.financialAnalysisService = financialAnalysisService;
    }
    
    /**
     * Get comprehensive financial analysis for a user
     * 
     * @param userId User ID
     * @param period Analysis period (week, month, quarter, year)
     * @return Financial analysis with AI recommendations
     */
    @GetMapping("/analysis/{userId}")
    public ResponseEntity<FinancialAnalysisResponse> getFinancialAnalysis(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "month") String period) {
        
        log.info("Received request for financial analysis - User: {}, Period: {}", userId, period);
        
        try {
            FinancialAnalysisResponse analysis = financialAnalysisService.analyzeFinances(userId, period);
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            log.error("Error generating financial analysis: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Trigger categorization of all transactions
     * 
     * @return Success message
     */
    @PostMapping("/categorize-all")
    public ResponseEntity<String> categorizeAllTransactions() {
        log.info("Received request to categorize all transactions");
        
        try {
            financialAnalysisService.categorizeAllTransactions();
            return ResponseEntity.ok("Categorization completed successfully");
            
        } catch (Exception e) {
            log.error("Error during categorization: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Categorization failed: " + e.getMessage());
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Financial Planner Service is running");
    }
}
