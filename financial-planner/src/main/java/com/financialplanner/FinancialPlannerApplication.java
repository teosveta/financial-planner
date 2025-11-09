package com.financialplanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FinancialPlannerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancialPlannerApplication.class, args);
    }
}
