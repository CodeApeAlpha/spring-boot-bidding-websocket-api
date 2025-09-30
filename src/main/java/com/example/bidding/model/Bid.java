// Package containing JPA entity models
package com.example.bidding.model;

// JPA annotations for ORM mapping
import jakarta.persistence.*;
// Bean Validation: minimum decimal constraint
import jakarta.validation.constraints.DecimalMin;
// Bean Validation: non-null constraint
import jakarta.validation.constraints.NotNull;
// BigDecimal for precise monetary values
import java.math.BigDecimal;
// LocalDateTime to store bid creation timestamp
import java.time.LocalDateTime;

// Marks this class as a JPA entity
@Entity
// Maps the entity to the "bids" table
@Table(name = "bids")
public class Bid {
    
    // Primary key identifier
    @Id
    // Auto-increment strategy appropriate for MySQL/H2
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Bidder name must be provided
    @NotNull(message = "Bidder name is required")
    // Column cannot be null in database
    @Column(nullable = false)
    private String bidderName;
    
    // Amount is required
    @NotNull(message = "Bid amount is required")
    // Minimum allowed amount is 0.01
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    // Store as DECIMAL(10,2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    // Bids must reference an item
    @NotNull(message = "Item ID is required")
    // Column cannot be null
    @Column(nullable = false)
    private Long itemId;
    
    // Timestamp of when the bid was created
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    // Whether this bid is currently winning
    @Column(nullable = false)
    private Boolean isWinning = false;
    
    // Constructors
    // Initialize default timestamp at creation
    public Bid() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Convenience constructor to populate fields
    public Bid(String bidderName, BigDecimal amount, Long itemId) {
        this();
        this.bidderName = bidderName;
        this.amount = amount;
        this.itemId = itemId;
    }
    
    // Getters and Setters
    // Returns the bid ID
    public Long getId() {
        return id;
    }
    
    // Sets the bid ID
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
    
    // Sets winning state
    public void setIsWinning(Boolean isWinning) {
        this.isWinning = isWinning;
    }
}
