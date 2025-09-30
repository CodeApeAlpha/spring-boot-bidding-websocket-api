// Package containing JPA entity models
package com.example.bidding.model;

// JPA annotations for ORM mapping
import jakarta.persistence.*;
// Bean Validation: minimum decimal constraint
import jakarta.validation.constraints.DecimalMin;
// Bean Validation: non-blank string constraint
import jakarta.validation.constraints.NotBlank;
// Bean Validation: non-null constraint
import jakarta.validation.constraints.NotNull;
// BigDecimal for monetary values
import java.math.BigDecimal;
// LocalDateTime to represent time fields
import java.time.LocalDateTime;

// Marks this class as a JPA entity
@Entity
// Maps the entity to the "items" table
@Table(name = "items")
public class Item {
    
    // Primary key identifier
    @Id
    // Auto-increment strategy appropriate for MySQL/H2
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Item must have a name
    @NotBlank(message = "Item name is required")
    // Column cannot be null
    @Column(nullable = false)
    private String name;
    
    // Longer free-form description
    @Column(columnDefinition = "TEXT")
    private String description;
    
    // Starting price must be provided
    @NotNull(message = "Starting price is required")
    // Minimum allowed starting price is 0.01
    @DecimalMin(value = "0.01", message = "Starting price must be greater than 0")
    // Store as DECIMAL(10,2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal startingPrice;
    
    // Current highest bid tracked for the item
    @Column(precision = 10, scale = 2)
    private BigDecimal currentHighestBid;
    
    // Auction start timestamp
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    // Auction end timestamp
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    // Store enum value as string
    @Enumerated(EnumType.STRING)
    // Column cannot be null
    @Column(nullable = false)
    private AuctionStatus status = AuctionStatus.ACTIVE;
    
    // Constructors
    // Initialize default start time on creation
    public Item() {
        this.startTime = LocalDateTime.now();
    }
    
    // Convenience constructor to populate fields
    public Item(String name, String description, BigDecimal startingPrice, LocalDateTime endTime) {
        this();
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentHighestBid = startingPrice;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    // Returns the item ID
    public Long getId() {
        return id;
    }
    
    // Sets the item ID
    public void setId(Long id) {
        this.id = id;
    }
    
    // Returns the item name
    public String getName() {
        return name;
    }
    
    // Sets the item name
    public void setName(String name) {
        this.name = name;
    }
    
    // Returns the item description
    public String getDescription() {
        return description;
    }
    
    // Sets the item description
    public void setDescription(String description) {
        this.description = description;
    }
    
    // Returns the starting price
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }
    
    // Sets the starting price
    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }
    
    // Returns the current highest bid
    public BigDecimal getCurrentHighestBid() {
        return currentHighestBid;
    }
    
    // Sets the current highest bid
    public void setCurrentHighestBid(BigDecimal currentHighestBid) {
        this.currentHighestBid = currentHighestBid;
    }
    
    // Returns the start time
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    // Sets the start time
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    // Returns the end time
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    // Sets the end time
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    // Returns the auction status
    public AuctionStatus getStatus() {
        return status;
    }
    
    // Sets the auction status
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}
