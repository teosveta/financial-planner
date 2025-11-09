package com.hackcash.financialplanner.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackcash.financialplanner.dto.TransactionDTO;
import com.hackcash.financialplanner.model.Transaction;
import com.hackcash.financialplanner.model.TransactionCategory;
import com.hackcash.financialplanner.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Transaction Service - CRUD operations and JSON import
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {
    
    private final TransactionRepository transactionRepository;
    private final TransactionCategorizationService categorizationService;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    
    @Value("${wallet.data.import.path}")
    private String importPath;
    
    @Value("${wallet.data.import.enabled}")
    private boolean importEnabled;
    
    /**
     * Import transactions from JSON file on startup
     */
    @PostConstruct
    @Transactional
    public void importTransactionsFromJSON() {
        if (!importEnabled) {
            log.info("JSON import is disabled");
            return;
        }
        
        try {
            Resource resource = resourceLoader.getResource(importPath);
            
            if (!resource.exists()) {
                log.warn("JSON import file not found: {}", importPath);
                return;
            }
            
            log.info("Importing transactions from: {}", importPath);
            
            JsonNode rootNode = objectMapper.readTree(resource.getInputStream());
            int imported = 0;
            
            // Handle different JSON structures
            JsonNode transactions = rootNode.isArray() ? rootNode : rootNode.get("transactions");
            
            if (transactions != null && transactions.isArray()) {
                for (JsonNode node : transactions) {
                    try {
                        Transaction transaction = parseTransactionFromJSON(node);
                        
                        // Auto-categorize if not already categorized
                        if (transaction.getCategory() == null) {
                            TransactionCategory category = categorizationService.categorize(transaction);
                            transaction.setCategory(category);
                        }
                        
                        transactionRepository.save(transaction);
                        imported++;
                    } catch (Exception e) {
                        log.error("Failed to import transaction: {}", e.getMessage());
                    }
                }
            }
            
            log.info("Successfully imported {} transactions", imported);
            
        } catch (Exception e) {
            log.error("Failed to import transactions from JSON: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Parse transaction from JSON node (flexible format support)
     */
    private Transaction parseTransactionFromJSON(JsonNode node) {
        Transaction transaction = new Transaction();
        
        // Merchant name (try different field names)
        String merchantName = getStringValue(node, "merchantName", "merchant", "description", "sender", "receiver");
        transaction.setMerchantName(merchantName != null ? merchantName : "Unknown");
        
        // Description
        String description = getStringValue(node, "description", "details", "memo");
        transaction.setDescription(description);
        
        // Amount
        BigDecimal amount = getBigDecimalValue(node, "amount", "value");
        transaction.setAmount(amount != null ? amount : BigDecimal.ZERO);
        
        // Transaction date
        LocalDateTime transactionDate = getDateTimeValue(node, "transactionDate", "date", "timestamp", "created");
        transaction.setTransactionDate(transactionDate != null ? transactionDate : LocalDateTime.now());
        
        // Category (optional)
        String categoryStr = getStringValue(node, "category", "type");
        if (categoryStr != null) {
            try {
                transaction.setCategory(TransactionCategory.fromString(categoryStr));
            } catch (Exception e) {
                log.debug("Could not parse category: {}", categoryStr);
            }
        }
        
        // Wallet and User IDs
        transaction.setWalletId(getStringValue(node, "walletId", "wallet_id"));
        transaction.setUserId(getStringValue(node, "userId", "user_id", "ownerId"));
        
        return transaction;
    }
    
    /**
     * Helper methods to extract values from JSON with fallback field names
     */
    private String getStringValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                return node.get(fieldName).asText();
            }
        }
        return null;
    }
    
    private BigDecimal getBigDecimalValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                return new BigDecimal(node.get(fieldName).asText());
            }
        }
        return null;
    }
    
    private LocalDateTime getDateTimeValue(JsonNode node, String... fieldNames) {
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
        };
        
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                String dateStr = node.get(fieldName).asText();
                
                for (DateTimeFormatter formatter : formatters) {
                    try {
                        return LocalDateTime.parse(dateStr, formatter);
                    } catch (Exception e) {
                        // Try next formatter
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Create new transaction with auto-categorization
     */
    @Transactional
    public Transaction createTransaction(TransactionDTO dto) {
        Transaction transaction = Transaction.builder()
            .merchantName(dto.getMerchantName())
            .description(dto.getDescription())
            .amount(dto.getAmount())
            .transactionDate(dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDateTime.now())
            .walletId(dto.getWalletId())
            .userId(dto.getUserId())
            .build();
        
        // Auto-categorize if category not provided
        if (dto.getCategory() == null || dto.getCategory().isEmpty()) {
            TransactionCategory category = categorizationService.categorize(transaction);
            transaction.setCategory(category);
        } else {
            transaction.setCategory(TransactionCategory.fromString(dto.getCategory()));
        }
        
        return transactionRepository.save(transaction);
    }
    
    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
    /**
     * Get transaction by ID
     */
    public Transaction getTransactionById(UUID id) {
        return transactionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
    }
    
    /**
     * Delete transaction
     */
    @Transactional
    public void deleteTransaction(UUID id) {
        transactionRepository.deleteById(id);
    }
    
    /**
     * Get recent transactions
     */
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByTransactionDateDesc();
    }
}
