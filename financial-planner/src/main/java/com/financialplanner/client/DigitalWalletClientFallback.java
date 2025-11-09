package com.financialplanner.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Fallback implementation for Digital Wallet client
 * Used when the wallet service is unavailable
 */
@Component
@Slf4j
public class DigitalWalletClientFallback implements DigitalWalletClient {

    @Override
    public List<Map<String, Object>> getUserTransactions(String userId) {
        log.warn("Digital Wallet service unavailable, returning empty list for user: {}", userId);
        return List.of();
    }

    @Override
    public List<Map<String, Object>> getUserTransactionsByPeriod(
            String userId, String startDate, String endDate) {
        log.warn("Digital Wallet service unavailable, returning empty list for user: {} (period: {} to {})",
                userId, startDate, endDate);
        return List.of();
    }

    @Override
    public Map<String, Object> getUserWalletInfo(String userId) {
        log.warn("Digital Wallet service unavailable, returning empty info for user: {}", userId);
        return Map.of("error", "Service unavailable", "userId", userId);
    }

    @Override
    public Map<String, Object> registerWebhook(Map<String, String> webhookConfig) {
        log.warn("Digital Wallet service unavailable, webhook registration failed");
        return Map.of("success", false, "message", "Service unavailable");
    }

    @Override
    public Map<String, Object> checkHealth() {
        log.warn("Digital Wallet service unavailable");
        return Map.of("status", "DOWN");
    }
}
