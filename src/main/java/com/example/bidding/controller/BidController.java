package com.example.bidding.controller;

import com.example.bidding.dto.request.BidRequest;
import com.example.bidding.dto.response.BidResponse;
import com.example.bidding.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "*")
@Tag(name = "Bid Management", description = "APIs for managing bids on auction items")
public class BidController {
    
    private static final Logger logger = LoggerFactory.getLogger(BidController.class);
    
    @Autowired
    private BidService bidService;
    
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    @Operation(summary = "Place a new bid", description = "Place a bid on an active auction item (BUYER role only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bid placed successfully",
                content = @Content(schema = @Schema(implementation = BidResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid bid request"),
        @ApiResponse(responseCode = "403", description = "Access denied - only BUYER role can place bids"),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "409", description = "Auction not active or bid too low")
    })
    public ResponseEntity<BidResponse> placeBid(@Valid @RequestBody BidRequest bidRequest) {
        logger.info("Received bid request for item {} by {}", bidRequest.getItemId(), bidRequest.getBidderName());
        BidResponse response = bidService.placeBid(bidRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/item/{itemId}")
    @Operation(summary = "Get bids for an item", description = "Retrieve all bids for a specific auction item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bids retrieved successfully",
                content = @Content(schema = @Schema(implementation = BidResponse.class))),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<List<BidResponse>> getBidsByItemId(
            @Parameter(description = "ID of the item to get bids for") 
            @PathVariable Long itemId) {
        
        logger.debug("Retrieving bids for item: {}", itemId);
        List<BidResponse> bids = bidService.getBidsByItemId(itemId);
        return ResponseEntity.ok(bids);
    }
    
    @GetMapping("/item/{itemId}/winning")
    @Operation(summary = "Get winning bid for an item", description = "Retrieve the current winning bid for a specific item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Winning bid retrieved successfully",
                content = @Content(schema = @Schema(implementation = BidResponse.class))),
        @ApiResponse(responseCode = "404", description = "Item not found or no winning bid")
    })
    public ResponseEntity<BidResponse> getWinningBid(
            @Parameter(description = "ID of the item to get winning bid for") 
            @PathVariable Long itemId) {
        
        logger.debug("Retrieving winning bid for item: {}", itemId);
        BidResponse winningBid = bidService.getWinningBid(itemId);
        if (winningBid != null) {
            return ResponseEntity.ok(winningBid);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/item/{itemId}/count")
    @Operation(summary = "Get bid count for an item", description = "Get the total number of bids placed on an item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bid count retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Long> getBidCount(
            @Parameter(description = "ID of the item to count bids for") 
            @PathVariable Long itemId) {
        
        logger.debug("Retrieving bid count for item: {}", itemId);
        Long count = bidService.getBidCount(itemId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/item/{itemId}/top")
    @Operation(summary = "Get top bids for an item", description = "Retrieve the highest bids for a specific item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Top bids retrieved successfully",
                content = @Content(schema = @Schema(implementation = BidResponse.class))),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<List<BidResponse>> getTopBids(
            @Parameter(description = "ID of the item to get top bids for") 
            @PathVariable Long itemId,
            @Parameter(description = "Maximum number of top bids to return") 
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("Retrieving top {} bids for item: {}", limit, itemId);
        List<BidResponse> topBids = bidService.getTopBids(itemId, limit);
        return ResponseEntity.ok(topBids);
    }
    
    @GetMapping("/bidder/{bidderName}")
    @Operation(summary = "Get bids by bidder", description = "Retrieve all bids placed by a specific bidder")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bids retrieved successfully",
                content = @Content(schema = @Schema(implementation = BidResponse.class)))
    })
    public ResponseEntity<List<BidResponse>> getBidsByBidderName(
            @Parameter(description = "Name of the bidder") 
            @PathVariable String bidderName) {
        
        logger.debug("Retrieving bids by bidder: {}", bidderName);
        List<BidResponse> bids = bidService.getBidsByBidderName(bidderName);
        return ResponseEntity.ok(bids);
    }
    
    @GetMapping
    @Operation(summary = "Get all bids", description = "Retrieve all bids across all items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All bids retrieved successfully",
                content = @Content(schema = @Schema(implementation = BidResponse.class)))
    })
    public ResponseEntity<List<BidResponse>> getAllBids() {
        logger.debug("Retrieving all bids");
        List<BidResponse> bids = bidService.getAllBids();
        return ResponseEntity.ok(bids);
    }
}