package com.financialplanner.config;

import com.financialplanner.dto.TransactionDTO;
import com.financialplanner.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final TransactionService transactionService;

    @Override
    public void run(String... args) {
        log.info("Initializing sample transaction data...");

        List<TransactionDTO.Request> sampleTransactions = List.of(
                // Food transactions
                TransactionDTO.Request.builder()
                        .description("Dinner with family")
                        .merchantName("McDonald's")
                        .amount(new BigDecimal("45.50"))
                        .transactionDate(LocalDateTime.now().minusDays(2))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Morning coffee")
                        .merchantName("Starbucks")
                        .amount(new BigDecimal("12.75"))
                        .transactionDate(LocalDateTime.now().minusDays(5))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Weekly groceries")
                        .merchantName("Whole Foods Market")
                        .amount(new BigDecimal("156.30"))
                        .transactionDate(LocalDateTime.now().minusDays(7))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Pizza night")
                        .merchantName("Domino's Pizza")
                        .amount(new BigDecimal("32.99"))
                        .transactionDate(LocalDateTime.now().minusDays(10))
                        .build(),

                // Entertainment
                TransactionDTO.Request.builder()
                        .description("Monthly subscription")
                        .merchantName("Netflix")
                        .amount(new BigDecimal("15.99"))
                        .transactionDate(LocalDateTime.now().minusDays(1))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Movie tickets")
                        .merchantName("AMC Theaters")
                        .amount(new BigDecimal("38.00"))
                        .transactionDate(LocalDateTime.now().minusDays(14))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Music streaming")
                        .merchantName("Spotify Premium")
                        .amount(new BigDecimal("9.99"))
                        .transactionDate(LocalDateTime.now().minusDays(3))
                        .build(),

                // Transport
                TransactionDTO.Request.builder()
                        .description("Ride to airport")
                        .merchantName("Uber")
                        .amount(new BigDecimal("45.20"))
                        .transactionDate(LocalDateTime.now().minusDays(6))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Gas refill")
                        .merchantName("Shell Gas Station")
                        .amount(new BigDecimal("68.50"))
                        .transactionDate(LocalDateTime.now().minusDays(8))
                        .build(),

                // Shopping
                TransactionDTO.Request.builder()
                        .description("New headphones")
                        .merchantName("Best Buy")
                        .amount(new BigDecimal("89.99"))
                        .transactionDate(LocalDateTime.now().minusDays(12))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Online shopping")
                        .merchantName("Amazon")
                        .amount(new BigDecimal("124.75"))
                        .transactionDate(LocalDateTime.now().minusDays(4))
                        .build(),

                // Bills
                TransactionDTO.Request.builder()
                        .description("Internet service")
                        .merchantName("Comcast Xfinity")
                        .amount(new BigDecimal("79.99"))
                        .transactionDate(LocalDateTime.now().minusDays(15))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Mobile phone bill")
                        .merchantName("Verizon Wireless")
                        .amount(new BigDecimal("65.00"))
                        .transactionDate(LocalDateTime.now().minusDays(9))
                        .build(),

                // Health
                TransactionDTO.Request.builder()
                        .description("Gym membership")
                        .merchantName("24 Hour Fitness")
                        .amount(new BigDecimal("49.99"))
                        .transactionDate(LocalDateTime.now().minusDays(11))
                        .build(),
                TransactionDTO.Request.builder()
                        .description("Prescription medication")
                        .merchantName("CVS Pharmacy")
                        .amount(new BigDecimal("23.50"))
                        .transactionDate(LocalDateTime.now().minusDays(13))
                        .build()
        );

        sampleTransactions.forEach(transaction -> {
            try {
                transactionService.createTransaction(transaction);
            } catch (Exception e) {
                log.error("Error creating sample transaction: {}", e.getMessage());
            }
        });

        log.info("Sample data initialization complete. Created {} transactions.", 
                sampleTransactions.size());
    }
}
