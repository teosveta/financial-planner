package com.hackcash.financialplanner.repository;

import com.hackcash.financialplanner.model.Transaction;
import com.hackcash.financialplanner.model.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    // Find transactions by date range
    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Find transactions by category
    List<Transaction> findByCategory(TransactionCategory category);
    
    // Find transactions by category and date range
    List<Transaction> findByCategoryAndTransactionDateBetween(
        TransactionCategory category, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    // Get total spending by category
    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
           "WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.category != 'INCOME' AND t.category != 'SAVINGS' " +
           "GROUP BY t.category")
    List<Object[]> getTotalSpendingByCategory(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Get total income in period
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
           "WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.category = 'INCOME'")
    BigDecimal getTotalIncome(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Get total expenses in period
    @Query("SELECT SUM(t.amount) FROM Transaction t " +
           "WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND t.category != 'INCOME' AND t.category != 'SAVINGS'")
    BigDecimal getTotalExpenses(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Get transaction count by category
    @Query("SELECT t.category, COUNT(t) FROM Transaction t " +
           "WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category")
    List<Object[]> getTransactionCountByCategory(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Find recent transactions
    List<Transaction> findTop10ByOrderByTransactionDateDesc();
    
    // Find transactions by user
    List<Transaction> findByUserId(String userId);
}
