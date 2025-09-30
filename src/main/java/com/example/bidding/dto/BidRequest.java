// Package containing Data Transfer Objects (DTOs)
package com.example.bidding.dto;

// Swagger schema annotations for API documentation
import io.swagger.v3.oas.annotations.media.Schema;
// Bean Validation: minimum decimal constraint
import jakarta.validation.constraints.DecimalMin;
// Bean Validation: non-blank string constraint
import jakarta.validation.constraints.NotBlank;
// Bean Validation: non-null constraint
import jakarta.validation.constraints.NotNull;
// BigDecimal for precise monetary values
import java.math.BigDecimal;

// Describes this DTO in generated API docs
@Schema(description = "Request object for placing a bid on an auction item")
public class BidRequest {
    
    // Describes the bidderName field in API docs
    @Schema(description = "Name of the bidder", example = "John Doe", required = true)
    // Must not be blank
    @NotBlank(message = "Bidder name is required")
    // Stores the name of the person placing the bid
    private String bidderName;
    
    // Describes the amount field in API docs
    @Schema(description = "Bid amount in dollars", example = "150.00", required = true)
    // Amount must be present
    @NotNull(message = "Bid amount is required")
    // Amount must be at least 0.01
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    // Stores the bid amount
    private BigDecimal amount;
    
    // Describes the itemId field in API docs
    @Schema(description = "ID of the auction item to bid on", example = "1", required = true)
    // Must reference an existing item
    @NotNull(message = "Item ID is required")
    // Stores the target auction item's ID
    private Long itemId;
    
    // Constructors
    // No-arg constructor required by frameworks
    public BidRequest() {}
    
    // Convenience constructor to populate all fields
    public BidRequest(String bidderName, BigDecimal amount, Long itemId) {
        // Initialize bidderName
        this.bidderName = bidderName;
        // Initialize amount
        this.amount = amount;
        // Initialize itemId
        this.itemId = itemId;
    }
    
    // Getters and Setters
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
    
    // Returns the target item ID
    public Long getItemId() {
        return itemId;
    }
    
    // Sets the target item ID
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
}
