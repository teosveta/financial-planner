package com.paysafe.hackcash.financialplanner.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    private UUID id;
    
    @JsonProperty("owner_id")
    private UUID ownerId;
    
    private String sender;
    private String receiver;
    private BigDecimal amount;
    
    @JsonProperty("balance_left")
    private BigDecimal balanceLeft;
    
    private String currency;
    private String type;
    private String status;
    private String description;
    
    @JsonProperty("failure_reason")
    private String failureReason;
    
    @JsonProperty("created_on")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdOn;
    
    // AI-enriched fields
    private String category;
    private String merchantName;
    private Double confidenceScore;
}
