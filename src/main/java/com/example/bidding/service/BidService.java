// Package containing service layer classes
package com.example.bidding.service;

// DTO carrying inputs for placing bids
import com.example.bidding.dto.BidRequest;
// DTO returned after bid operations
import com.example.bidding.dto.BidResponse;
// WebSocket message wrapper for broadcasting updates
import com.example.bidding.dto.WebSocketMessage;
// Enum representing auction status values
import com.example.bidding.model.AuctionStatus;
// Entity representing a bid record
import com.example.bidding.model.Bid;
// Entity representing an auction item
import com.example.bidding.model.Item;
// Repository for bid persistence operations
import com.example.bidding.repository.JdbcBidRepository;
// Repository for item persistence operations
import com.example.bidding.repository.JdbcItemRepository;
// Spring dependency injection
import org.springframework.beans.factory.annotation.Autowired;
// Template to send STOMP messages to clients
import org.springframework.messaging.simp.SimpMessagingTemplate;
// Marks this class as a Spring service component
import org.springframework.stereotype.Service;
// Transactional annotation to wrap public methods in DB transactions
import org.springframework.transaction.annotation.Transactional;

// BigDecimal for monetary arithmetic
import java.math.BigDecimal;
// Timestamp utilities
import java.time.LocalDateTime;
// Collections used in method responses
import java.util.List;
// Optional wrapper for possibly missing entities
import java.util.Optional;
// Stream utilities to map entities to DTOs
import java.util.stream.Collectors;

// Service component managed by Spring
@Service
// Enable transactional behavior for public methods
@Transactional
public class BidService {
    
    // Inject bid repository
    @Autowired
    private JdbcBidRepository bidRepository;
    
    // Inject item repository
    @Autowired
    private JdbcItemRepository itemRepository;
    
    // Inject messaging template for WebSocket updates
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    
    // Place a new bid after validating business rules
    public BidResponse placeBid(BidRequest bidRequest) {
        // Validate item exists and is active
        Optional<Item> itemOpt = itemRepository.findById(bidRequest.getItemId());
        if (itemOpt.isEmpty()) {
            throw new IllegalArgumentException("Item not found");
        }
        
        Item item = itemOpt.get();
        if (item.getStatus() != AuctionStatus.ACTIVE) {
            throw new IllegalArgumentException("Auction is not active");
        }
        
        if (LocalDateTime.now().isAfter(item.getEndTime())) {
            throw new IllegalArgumentException("Auction has ended");
        }
        
        // Validate bid amount
        BigDecimal minimumBid = item.getCurrentHighestBid() != null ? 
            item.getCurrentHighestBid().add(new BigDecimal("0.01")) : 
            item.getStartingPrice();
            
        if (bidRequest.getAmount().compareTo(minimumBid) < 0) {
            throw new IllegalArgumentException("Bid amount must be at least " + minimumBid);
        }
        
        // Create and save bid
        Bid bid = new Bid(bidRequest.getBidderName(), bidRequest.getAmount(), bidRequest.getItemId());
        bid = bidRepository.save(bid);
        
        // Update item's current highest bid
        item.setCurrentHighestBid(bidRequest.getAmount());
        itemRepository.save(item);
        
        // Mark previous winning bid as not winning
        Optional<Bid> previousWinningBid = bidRepository.findWinningBidByItemId(bidRequest.getItemId());
        if (previousWinningBid.isPresent()) {
            Bid prevBid = previousWinningBid.get();
            prevBid.setIsWinning(false);
            bidRepository.save(prevBid);
        }
        
        // Mark new bid as winning
        bid.setIsWinning(true);
        bid = bidRepository.save(bid);
        
        // Send real-time WebSocket updates
        BidResponse bidResponse = convertToResponse(bid);
        
        // Send item-specific update
        WebSocketMessage itemMessage = new WebSocketMessage("NEW_BID", bidResponse, bidRequest.getItemId());
        messagingTemplate.convertAndSend("/topic/bids/" + bidRequest.getItemId(), itemMessage);
        
        // Send general live feed update
        WebSocketMessage liveFeedMessage = new WebSocketMessage("BID_UPDATE", bidResponse);
        messagingTemplate.convertAndSend("/topic/auctions", liveFeedMessage);
        
        return bidResponse;
    }
    
    // Retrieve bids for a given item, mapped to DTOs
    public List<BidResponse> getBidsByItemId(Long itemId) {
        List<Bid> bids = bidRepository.findByItemIdOrderByAmountDesc(itemId);
        return bids.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // Retrieve the current winning bid for an item
    public BidResponse getWinningBid(Long itemId) {
        Optional<Bid> winningBid = bidRepository.findWinningBidByItemId(itemId);
        return winningBid.map(this::convertToResponse).orElse(null);
    }
    
    // Count number of bids placed on an item
    public Long getBidCount(Long itemId) {
        return bidRepository.countBidsByItemId(itemId);
    }
    
    // Retrieve top bids limited by the specified count
    public List<BidResponse> getTopBids(Long itemId, int limit) {
        List<Bid> bids = bidRepository.findTopBidsByItemId(itemId);
        return bids.stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // Retrieve all bids from all items for shared live feed
    public List<BidResponse> getAllBids() {
        List<Bid> bids = bidRepository.findAll();
        return bids.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    // Helper to map a Bid entity to a BidResponse DTO
    private BidResponse convertToResponse(Bid bid) {
        return new BidResponse(
            bid.getId(),
            bid.getBidderName(),
            bid.getAmount(),
            bid.getItemId(),
            bid.getTimestamp(),
            bid.getIsWinning()
        );
    }
}
