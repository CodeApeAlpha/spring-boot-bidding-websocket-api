// Package containing Data Transfer Objects (DTOs)
package com.example.bidding.dto;

// Import enum representing auction status values
import com.example.bidding.model.AuctionStatus;

// Generic WebSocket message wrapper sent to clients
public class WebSocketMessage {
    
    // Type identifier for the message (e.g., NEW_BID, AUCTION_UPDATE)
    private String type;
    // Payload carried with the message (can be any type)
    private Object data;
    // Optional: item ID that this message relates to
    private Long itemId;
    // Optional: current status of the auction
    private AuctionStatus status;
    
    // Constructors
    // No-arg constructor for frameworks
    public WebSocketMessage() {}
    
    // Construct a message with type and payload
    public WebSocketMessage(String type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    // Construct a message with an associated item ID
    public WebSocketMessage(String type, Object data, Long itemId) {
        this.type = type;
        this.data = data;
        this.itemId = itemId;
    }
    
    // Construct a message with an associated item ID and status
    public WebSocketMessage(String type, Object data, Long itemId, AuctionStatus status) {
        this.type = type;
        this.data = data;
        this.itemId = itemId;
        this.status = status;
    }
    
    // Getters and Setters
    // Returns the message type
    public String getType() {
        return type;
    }
    
    // Sets the message type
    public void setType(String type) {
        this.type = type;
    }
    
    // Returns the payload
    public Object getData() {
        return data;
    }
    
    // Sets the payload
    public void setData(Object data) {
        this.data = data;
    }
    
    // Returns the associated item ID
    public Long getItemId() {
        return itemId;
    }
    
    // Sets the associated item ID
    public void setItemId(Long itemId) {
        this.itemId = itemId;
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
