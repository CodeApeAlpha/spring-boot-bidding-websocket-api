package com.example.bidding.service;

import com.example.bidding.dto.request.BidRequest;
import com.example.bidding.dto.response.BidResponse;
import com.example.bidding.dto.WebSocketMessage;
import com.example.bidding.entity.AuctionStatus;
import com.example.bidding.entity.Bid;
import com.example.bidding.entity.Item;
import com.example.bidding.exception.BusinessException;
import com.example.bidding.exception.ResourceNotFoundException;
import com.example.bidding.repository.BidRepository;
import com.example.bidding.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(BidService.class);
    
    @Autowired
    private BidRepository bidRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    public BidResponse placeBid(BidRequest bidRequest) {
        logger.info("Placing bid for item {} by {}", bidRequest.getItemId(), bidRequest.getBidderName());
        
        // Validate item exists and is active
        Item item = itemRepository.findById(bidRequest.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + bidRequest.getItemId()));
        
        if (item.getStatus() != AuctionStatus.ACTIVE) {
            throw new BusinessException("Auction is not active");
        }
        
        if (LocalDateTime.now().isAfter(item.getEndTime())) {
            throw new BusinessException("Auction has ended");
        }
        
        // Validate bid amount
        BigDecimal minimumBid = item.getCurrentHighestBid() != null ? 
            item.getCurrentHighestBid().add(new BigDecimal("0.01")) : 
            item.getStartingPrice();
            
        if (bidRequest.getAmount().compareTo(minimumBid) < 0) {
            throw new BusinessException("Bid amount must be at least " + minimumBid);
        }
        
        // Create and save bid
        Bid bid = new Bid(bidRequest.getBidderName(), bidRequest.getAmount(), item);
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
        
        logger.info("Bid placed successfully: {}", bidResponse.getId());
        return bidResponse;
    }
    
    @Transactional(readOnly = true)
    public List<BidResponse> getBidsByItemId(Long itemId) {
        logger.debug("Retrieving bids for item: {}", itemId);
        List<Bid> bids = bidRepository.findByItemIdOrderByAmountDesc(itemId);
        return bids.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BidResponse getWinningBid(Long itemId) {
        logger.debug("Retrieving winning bid for item: {}", itemId);
        Optional<Bid> winningBid = bidRepository.findWinningBidByItemId(itemId);
        return winningBid.map(this::convertToResponse).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public Long getBidCount(Long itemId) {
        logger.debug("Counting bids for item: {}", itemId);
        return bidRepository.countBidsByItemId(itemId);
    }
    
    @Transactional(readOnly = true)
    public List<BidResponse> getTopBids(Long itemId, int limit) {
        logger.debug("Retrieving top {} bids for item: {}", limit, itemId);
        List<Bid> bids = bidRepository.findHighestBidsByItemId(itemId);
        return bids.stream()
                .limit(limit)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BidResponse> getAllBids() {
        logger.debug("Retrieving all bids");
        List<Bid> bids = bidRepository.findAll();
        return bids.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BidResponse> getBidsByBidderName(String bidderName) {
        logger.debug("Retrieving bids by bidder: {}", bidderName);
        List<Bid> bids = bidRepository.findByBidderNameOrderByTimestampDesc(bidderName);
        return bids.stream()
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