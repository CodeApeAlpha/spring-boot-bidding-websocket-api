package com.example.bidding.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidResponse {
    
    private Long id;
    private String bidderName;
    private BigDecimal amount;
    private Long itemId;
    private LocalDateTime timestamp;
    private Boolean isWinning;
    
    // Constructors
    public BidResponse() {}
    
    public BidResponse(Long id, String bidderName, BigDecimal amount, Long itemId, 
                      LocalDateTime timestamp, Boolean isWinning) {
        this.id = id;
        this.bidderName = bidderName;
        this.amount = amount;
        this.itemId = itemId;
        this.timestamp = timestamp;
        this.isWinning = isWinning;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBidderName() {
        return bidderName;
    }
    
    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Long getItemId() {
        return itemId;
    }
    
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Boolean getIsWinning() {
        return isWinning;
    }
    
    public void setIsWinning(Boolean isWinning) {
        this.isWinning = isWinning;
    }
}
