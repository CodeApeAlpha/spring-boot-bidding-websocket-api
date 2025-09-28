package com.example.bidding.service;

import com.example.bidding.dto.BidRequest;
import com.example.bidding.dto.BidResponse;
import com.example.bidding.dto.WebSocketMessage;
import com.example.bidding.model.AuctionStatus;
import com.example.bidding.model.Bid;
import com.example.bidding.model.Item;
import com.example.bidding.repository.BidRepository;
import com.example.bidding.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class BidService {
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
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
        
        // Send WebSocket update
        BidResponse bidResponse = convertToResponse(bid);
        WebSocketMessage message = new WebSocketMessage("NEW_BID", bidResponse, bidRequest.getItemId());
        messagingTemplate.convertAndSend("/topic/bids/" + bidRequest.getItemId(), message);
        
        // Send general update for all active auctions
        WebSocketMessage generalMessage = new WebSocketMessage("BID_UPDATE", bidResponse);
        messagingTemplate.convertAndSend("/topic/auctions", generalMessage);
        
        return bidResponse;
    }
    
    public List<BidResponse> getBidsByItemId(Long itemId) {
        List<Bid> bids = bidRepository.findByItemIdOrderByAmountDesc(itemId);
        return bids.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public BidResponse getWinningBid(Long itemId) {
        Optional<Bid> winningBid = bidRepository.findWinningBidByItemId(itemId);
        return winningBid.map(this::convertToResponse).orElse(null);
    }
    
    public Long getBidCount(Long itemId) {
        return bidRepository.countBidsByItemId(itemId);
    }
    
    public List<BidResponse> getTopBids(Long itemId, int limit) {
        List<Bid> bids = bidRepository.findTopBidsByItemId(itemId);
        return bids.stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
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
