package com.paysafe.hackcash.financialplanner.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paysafe.hackcash.financialplanner.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TransactionRepository {
    
    private final ObjectMapper objectMapper;
    private final String filePath;
    
    public TransactionRepository(
            ObjectMapper objectMapper,
            @Value("${data.transactions.file-path}") String filePath) {
        this.objectMapper = objectMapper;
        this.filePath = filePath;
    }
    
    /**
     * Load all transactions from JSON file
     */
    public List<Transaction> loadTransactions() {
        log.info("Loading transactions from: {}", filePath);
        
        try {
            File file = new File(filePath);
            
            if (!file.exists()) {
                log.warn("Transactions file not found: {}. Using empty list.", filePath);
                return new ArrayList<>();
            }
            
            List<Transaction> transactions = objectMapper.readValue(
                file, 
                new TypeReference<List<Transaction>>() {}
            );
            
            log.info("Loaded {} transactions successfully", transactions.size());
            return transactions;
            
        } catch (IOException e) {
            log.error("Error loading transactions from file: {}", e.getMessage());
            throw new RuntimeException("Failed to load transactions", e);
        }
    }
    
    /**
     * Load transactions for a specific user
     */
    public List<Transaction> loadTransactionsByUserId(UUID userId) {
        List<Transaction> allTransactions = loadTransactions();
        
        return allTransactions.stream()
                .filter(tx -> tx.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }
    
    /**
     * Save categorized transactions back to file
     */
    public void saveTransactions(List<Transaction> transactions) {
        log.info("Saving {} transactions to: {}", transactions.size(), filePath);
        
        try {
            File file = new File(filePath);
            
            // Ensure parent directory exists
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, transactions);
            
            log.info("Transactions saved successfully");
            
        } catch (IOException e) {
            log.error("Error saving transactions to file: {}", e.getMessage());
            throw new RuntimeException("Failed to save transactions", e);
        }
    }
}
