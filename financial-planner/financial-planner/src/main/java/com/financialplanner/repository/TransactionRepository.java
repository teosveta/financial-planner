package com.financialplanner.repository;

import com.financialplanner.model.Transaction;
import com.financialplanner.model.Transaction.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTransactionDateBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findByCategory(TransactionCategory category);

    @Query("SELECT t FROM Transaction t WHERE t.transactionDate >= :start AND t.transactionDate < :end")
    List<Transaction> findTransactionsInPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.category = :category " +
           "AND t.transactionDate >= :start AND t.transactionDate < :end")
    BigDecimal sumAmountByCategoryInPeriod(
            @Param("category") TransactionCategory category,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionDate >= :start AND t.transactionDate < :end")
    BigDecimal sumTotalInPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
