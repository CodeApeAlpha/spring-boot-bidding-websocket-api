package com.example.bidding.controller;

import com.example.bidding.dto.WebSocketMessage;
import com.example.bidding.model.AuctionStatus;
import com.example.bidding.model.Item;
import com.example.bidding.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketController {
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/auctions/status")
    @SendTo("/topic/auctions/status")
    public WebSocketMessage getAuctionStatus() {
        List<Item> activeItems = itemService.getActiveItems();
        return new WebSocketMessage("AUCTION_STATUS", activeItems);
    }
    
    @MessageMapping("/auctions/join")
    public void joinAuction(Long itemId) {
        // This method can be used to handle users joining specific auction rooms
        // For now, we'll just acknowledge the join
        WebSocketMessage message = new WebSocketMessage("JOINED_AUCTION", "Successfully joined auction", itemId);
        messagingTemplate.convertAndSend("/topic/auctions/" + itemId, message);
    }
    
    // Method to broadcast auction status updates
    public void broadcastAuctionUpdate(Item item) {
        WebSocketMessage message = new WebSocketMessage("AUCTION_UPDATE", item, item.getId(), item.getStatus());
        messagingTemplate.convertAndSend("/topic/auctions", message);
        messagingTemplate.convertAndSend("/topic/auctions/" + item.getId(), message);
    }
    
    // Method to broadcast when an auction ends
    public void broadcastAuctionEnd(Item item) {
        WebSocketMessage message = new WebSocketMessage("AUCTION_ENDED", item, item.getId(), AuctionStatus.ENDED);
        messagingTemplate.convertAndSend("/topic/auctions", message);
        messagingTemplate.convertAndSend("/topic/auctions/" + item.getId(), message);
    }
}
