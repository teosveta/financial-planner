package com.hackcash.financialplanner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAnalysisDTO {
    private String category;
    private String categoryDisplayName;
    private String categoryIcon;
    private BigDecimal totalAmount;
    private Integer transactionCount;
    private Double percentage;
    private BigDecimal averageTransactionAmount;
    private Double recommendedPercentage;
}
