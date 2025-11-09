package com.financialplanner.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Feign Client for communicating with Digital Wallet microservice
 * 
 * This allows the Financial Planner to:
 * - Fetch transaction data from the wallet
 * - Get user information
 * - Subscribe to transaction events
 */
@FeignClient(
        name = "digital-wallet",
        url = "${digital-wallet.url:http://localhost:8080}",
        fallback = DigitalWalletClientFallback.class
)
public interface DigitalWalletClient {

    /**
     * Fetch user's transactions from Digital Wallet
     */
    @GetMapping("/api/wallet/transactions/{userId}")
    List<Map<String, Object>> getUserTransactions(@PathVariable String userId);

    /**
     * Fetch user's transactions for a specific period
     */
    @GetMapping("/api/wallet/transactions/{userId}/period")
    List<Map<String, Object>> getUserTransactionsByPeriod(
            @PathVariable String userId,
            @RequestParam String startDate,
            @RequestParam String endDate
    );

    /**
     * Fetch user wallet information
     */
    @GetMapping("/api/wallet/user/{userId}")
    Map<String, Object> getUserWalletInfo(@PathVariable String userId);

    /**
     * Register webhook for transaction notifications
     */
    @PostMapping("/api/wallet/webhook/register")
    Map<String, Object> registerWebhook(@RequestBody Map<String, String> webhookConfig);

    /**
     * Health check for Digital Wallet service
     */
    @GetMapping("/actuator/health")
    Map<String, Object> checkHealth();
}
