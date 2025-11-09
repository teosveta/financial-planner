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
public class RecommendationDTO {
    private String message;
    private Integer priority;
    private String category;
    private String source; // "AI" or "Rule-Based"
    private Boolean actionable;
    private BigDecimal potentialSavings;
}
