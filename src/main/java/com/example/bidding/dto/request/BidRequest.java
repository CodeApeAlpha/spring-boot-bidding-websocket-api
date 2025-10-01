package com.example.bidding.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class BidRequest {
    
    @NotBlank(message = "Bidder name is required")
    private String bidderName;
    
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Item ID is required")
    private Long itemId;
    
    public BidRequest() {}
    
    public BidRequest(String bidderName, BigDecimal amount, Long itemId) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.itemId = itemId;
    }
    
    // Getters and Setters
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
}
