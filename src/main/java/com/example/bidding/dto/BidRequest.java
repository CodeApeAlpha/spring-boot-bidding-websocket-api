package com.example.bidding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request object for placing a bid on an auction item")
public class BidRequest {
    
    @Schema(description = "Name of the bidder", example = "John Doe", required = true)
    @NotBlank(message = "Bidder name is required")
    private String bidderName;
    
    @Schema(description = "Bid amount in dollars", example = "150.00", required = true)
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    private BigDecimal amount;
    
    @Schema(description = "ID of the auction item to bid on", example = "1", required = true)
    @NotNull(message = "Item ID is required")
    private Long itemId;
    
    // Constructors
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
