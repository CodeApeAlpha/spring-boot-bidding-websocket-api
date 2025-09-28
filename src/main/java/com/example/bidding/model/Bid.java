package com.example.bidding.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Bidder name is required")
    @Column(nullable = false)
    private String bidderName;
    
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @NotNull(message = "Item ID is required")
    @Column(nullable = false)
    private Long itemId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private Boolean isWinning = false;
    
    // Constructors
    public Bid() {
        this.timestamp = LocalDateTime.now();
    }
    
    public Bid(String bidderName, BigDecimal amount, Long itemId) {
        this();
        this.bidderName = bidderName;
        this.amount = amount;
        this.itemId = itemId;
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
