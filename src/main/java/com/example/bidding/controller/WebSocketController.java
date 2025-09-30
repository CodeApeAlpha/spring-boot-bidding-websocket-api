// Package declaration for controller classes
package com.example.bidding.controller;

// DTO used to transport WebSocket payloads to clients
import com.example.bidding.dto.WebSocketMessage;
// Enum representing auction lifecycle states
import com.example.bidding.model.AuctionStatus;
// Entity representing an auction item
import com.example.bidding.model.Item;
// Service providing access to item data and business logic
import com.example.bidding.service.ItemService;
// Spring dependency injection
import org.springframework.beans.factory.annotation.Autowired;
// Maps incoming STOMP messages to handler methods
import org.springframework.messaging.handler.annotation.MessageMapping;
// Sends handler return values to a STOMP destination
import org.springframework.messaging.handler.annotation.SendTo;
// Template used to programmatically send messages to destinations
import org.springframework.messaging.simp.SimpMessagingTemplate;
// Marks this as a Spring messaging controller
import org.springframework.stereotype.Controller;

// Java util list for collections
import java.util.List;

// WebSocket/STOMP controller for real-time auction updates
@Controller
public class WebSocketController {
    
    // Inject item service to fetch item data for messages
    @Autowired
    private ItemService itemService;
    
    // Inject messaging template to broadcast messages
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Handle client messages sent to /app/auctions/status
    @MessageMapping("/auctions/status")
    // Broadcast the return value to subscribers of /topic/auctions/status
    @SendTo("/topic/auctions/status")
    public WebSocketMessage getAuctionStatus() {
        // Fetch active items and wrap them in a message payload
        List<Item> activeItems = itemService.getActiveItems();
        return new WebSocketMessage("AUCTION_STATUS", activeItems);
    }
    
    // Handle client messages sent to /app/auctions/join
    @MessageMapping("/auctions/join")
    public void joinAuction(Long itemId) {
        // Acknowledge join to the specific auction topic
        WebSocketMessage message = new WebSocketMessage("JOINED_AUCTION", "Successfully joined auction", itemId);
        messagingTemplate.convertAndSend("/topic/auctions/" + itemId, message);
    }
    
    // Programmatically broadcast auction status updates to all subscribers
    public void broadcastAuctionUpdate(Item item) {
        WebSocketMessage message = new WebSocketMessage("AUCTION_UPDATE", item, item.getId(), item.getStatus());
        messagingTemplate.convertAndSend("/topic/auctions", message);
        messagingTemplate.convertAndSend("/topic/auctions/" + item.getId(), message);
    }
    
    // Programmatically broadcast a message when an auction ends
    public void broadcastAuctionEnd(Item item) {
        WebSocketMessage message = new WebSocketMessage("AUCTION_ENDED", item, item.getId(), AuctionStatus.ENDED);
        messagingTemplate.convertAndSend("/topic/auctions", message);
        messagingTemplate.convertAndSend("/topic/auctions/" + item.getId(), message);
    }
}
