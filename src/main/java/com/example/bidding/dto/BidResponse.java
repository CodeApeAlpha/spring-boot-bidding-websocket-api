package com.example.bidding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Response object containing bid information")
public class BidResponse {
    
    @Schema(description = "Unique identifier of the bid", example = "1")
    private Long id;
    
    @Schema(description = "Name of the bidder", example = "John Doe")
    private String bidderName;
    
    @Schema(description = "Bid amount in dollars", example = "150.00")
    private BigDecimal amount;
    
    @Schema(description = "ID of the auction item", example = "1")
    private Long itemId;
    
    @Schema(description = "Timestamp when the bid was placed", example = "2023-12-01T10:30:00")
    private LocalDateTime timestamp;
    
    @Schema(description = "Whether this bid is currently winning", example = "true")
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
