package com.example.bidding.dto;

import com.example.bidding.model.AuctionStatus;

public class WebSocketMessage {
    
    private String type;
    private Object data;
    private Long itemId;
    private AuctionStatus status;
    
    // Constructors
    public WebSocketMessage() {}
    
    public WebSocketMessage(String type, Object data) {
        this.type = type;
        this.data = data;
    }
    
    public WebSocketMessage(String type, Object data, Long itemId) {
        this.type = type;
        this.data = data;
        this.itemId = itemId;
    }
    
    public WebSocketMessage(String type, Object data, Long itemId, AuctionStatus status) {
        this.type = type;
        this.data = data;
        this.itemId = itemId;
        this.status = status;
    }
    
    // Getters and Setters
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public Long getItemId() {
        return itemId;
    }
    
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    public AuctionStatus getStatus() {
        return status;
    }
    
    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}
