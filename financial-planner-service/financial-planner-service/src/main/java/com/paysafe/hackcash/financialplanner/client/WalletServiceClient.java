package com.paysafe.hackcash.financialplanner.client;

import com.paysafe.hackcash.financialplanner.model.Transaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Feign client for communicating with the main Hack-Cash wallet service
 */
@FeignClient(name = "wallet-service", url = "${wallet.service.url}")
public interface WalletServiceClient {
    
    /**
     * Get all transactions for a specific user
     */
    @GetMapping("/api/v1/transactions/user/{userId}")
    ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable UUID userId);
    
    /**
     * Get wallet balance for a user
     */
    @GetMapping("/api/v1/wallets/user/{userId}/balance")
    ResponseEntity<String> getUserBalance(@PathVariable UUID userId);
}
