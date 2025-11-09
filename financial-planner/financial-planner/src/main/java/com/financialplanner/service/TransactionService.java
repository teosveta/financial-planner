package com.financialplanner.service;

import com.financialplanner.dto.TransactionDTO;
import com.financialplanner.model.Transaction;
import com.financialplanner.model.Transaction.TransactionCategory;
import com.financialplanner.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionCategorizationService categorizationService;

    /**
     * Create a new transaction with automatic categorization
     */
    @Transactional
    public TransactionDTO.Response createTransaction(TransactionDTO.Request request) {
        log.info("Creating new transaction for merchant: {}", request.getMerchantName());

        // Auto-categorize the transaction using AI
        TransactionCategory category = categorizationService.categorize(
                request.getMerchantName(),
                request.getDescription(),
                request.getAmount()
        );

        Transaction transaction = Transaction.builder()
                .description(request.getDescription())
                .merchantName(request.getMerchantName())
                .amount(request.getAmount())
                .category(category)
                .transactionDate(request.getTransactionDate() != null 
                        ? request.getTransactionDate() 
                        : LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created with ID: {} and category: {}", saved.getId(), category);

        return mapToResponse(saved);
    }

    /**
     * Get all transactions
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO.Response> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get transaction by ID
     */
    @Transactional(readOnly = true)
    public TransactionDTO.Response getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return mapToResponse(transaction);
    }

    /**
     * Get transactions by category
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO.Response> getTransactionsByCategory(TransactionCategory category) {
        return transactionRepository.findByCategory(category).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get transactions in date range
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO.Response> getTransactionsByDateRange(
            LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByTransactionDateBetween(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete transaction
     */
    @Transactional
    public void deleteTransaction(Long id) {
        log.info("Deleting transaction with ID: {}", id);
        transactionRepository.deleteById(id);
    }

    /**
     * Map Transaction entity to Response DTO
     */
    private TransactionDTO.Response mapToResponse(Transaction transaction) {
        return TransactionDTO.Response.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .merchantName(transaction.getMerchantName())
                .amount(transaction.getAmount())
                .category(transaction.getCategory())
                .categoryDisplayName(transaction.getCategory().getDisplayName())
                .transactionDate(transaction.getTransactionDate())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
