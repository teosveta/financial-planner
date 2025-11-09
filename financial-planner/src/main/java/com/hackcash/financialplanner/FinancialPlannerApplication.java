package com.hackcash.financialplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Financial Planner Microservice - AI-Powered Transaction Analysis & Recommendations
 * Port: 8081
 * Integrates with Hack-Cash Digital Wallet (Port: 8080) via JSON data loading
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class FinancialPlannerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FinancialPlannerApplication.class, args);
    }
}
