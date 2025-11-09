package com.paysafe.hackcash.financialplanner.dto;

import com.paysafe.hackcash.financialplanner.model.SpendingCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorizationResult {
    
    private SpendingCategory category;
    private String merchantName;
    private Double confidenceScore;
    private String reasoning;
}
