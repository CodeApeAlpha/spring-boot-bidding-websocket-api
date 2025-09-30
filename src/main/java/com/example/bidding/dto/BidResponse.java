// Package containing Data Transfer Objects (DTOs)
package com.example.bidding.dto;

// Swagger schema annotations for API documentation
import io.swagger.v3.oas.annotations.media.Schema;
// BigDecimal for precise monetary values
import java.math.BigDecimal;
// LocalDateTime to represent bid timestamps
import java.time.LocalDateTime;

// Describes this DTO in generated API docs
@Schema(description = "Response object containing bid information")
public class BidResponse {
    
    // Bid ID used as a unique identifier
    @Schema(description = "Unique identifier of the bid", example = "1")
    private Long id;
    
    // Name of the person who placed the bid
    @Schema(description = "Name of the bidder", example = "John Doe")
    private String bidderName;
    
    // Monetary value of the bid
    @Schema(description = "Bid amount in dollars", example = "150.00")
    private BigDecimal amount;
    
    // Reference to the auction item ID
    @Schema(description = "ID of the auction item", example = "1")
    private Long itemId;
    
    // When the bid was created
    @Schema(description = "Timestamp when the bid was placed", example = "2023-12-01T10:30:00")
    private LocalDateTime timestamp;
    
    // Whether this bid is currently the winning bid
    @Schema(description = "Whether this bid is currently winning", example = "true")
    private Boolean isWinning;
    
    // Constructors
    // No-arg constructor required by frameworks
    public BidResponse() {}
    
    // Convenience constructor to populate all fields
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
    // Returns the unique bid ID
    public Long getId() {
        return id;
    }
    
    // Sets the unique bid ID
    public void setId(Long id) {
        this.id = id;
    }
    
    // Returns the bidder's name
    public String getBidderName() {
        return bidderName;
    }
    
    // Sets the bidder's name
    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }
    
    // Returns the bid amount
    public BigDecimal getAmount() {
        return amount;
    }
    
    // Sets the bid amount
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    // Returns the associated item ID
    public Long getItemId() {
        return itemId;
    }
    
    // Sets the associated item ID
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    // Returns the bid timestamp
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    // Sets the bid timestamp
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    // Returns whether this bid is currently winning
    public Boolean getIsWinning() {
        return isWinning;
    }
    
    // Sets whether this bid is currently winning
    public void setIsWinning(Boolean isWinning) {
        this.isWinning = isWinning;
    }
}
