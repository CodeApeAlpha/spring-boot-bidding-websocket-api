package com.example.bidding.controller;

import com.example.bidding.dto.WebSocketMessage;
import com.example.bidding.dto.response.ItemResponse;
import com.example.bidding.entity.AuctionStatus;
import com.example.bidding.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    @Autowired
    private ItemService itemService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/auctions/status")
    @SendTo("/topic/auctions/status")
    public WebSocketMessage getAuctionStatus() {
        logger.debug("WebSocket: Client requested auction status");
        List<ItemResponse> activeItems = itemService.getActiveItems();
        return new WebSocketMessage("AUCTION_STATUS", activeItems);
    }
    
    @MessageMapping("/auctions/join")
    public void joinAuction(Long itemId) {
        logger.debug("WebSocket: Client joined auction for item: {}", itemId);
        WebSocketMessage message = new WebSocketMessage("JOINED_AUCTION", "Successfully joined auction", itemId);
        messagingTemplate.convertAndSend("/topic/auctions/" + itemId, message);
    }
    
    public void broadcastAuctionUpdate(ItemResponse item) {
        logger.debug("WebSocket: Broadcasting auction update for item: {}", item.getId());
        WebSocketMessage message = new WebSocketMessage("AUCTION_UPDATE", item);
        messagingTemplate.convertAndSend("/topic/auctions", message);
    }
    
    public void broadcastBidUpdate(Long itemId, Object bidData) {
        logger.debug("WebSocket: Broadcasting bid update for item: {}", itemId);
        WebSocketMessage message = new WebSocketMessage("BID_UPDATE", bidData, itemId);
        messagingTemplate.convertAndSend("/topic/bids/" + itemId, message);
        messagingTemplate.convertAndSend("/topic/auctions", message);
    }
}