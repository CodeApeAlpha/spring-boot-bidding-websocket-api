package com.example.bidding.controller;

import com.example.bidding.dto.BidRequest;
import com.example.bidding.dto.BidResponse;
import com.example.bidding.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "*")
@Tag(name = "Bid Management", description = "APIs for managing bids on auction items")
public class BidController {
    
    @Autowired
    private BidService bidService;
    
    @Operation(summary = "Place a new bid", description = "Place a bid on an auction item. The bid must be higher than the current highest bid.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bid placed successfully",
                    content = @Content(schema = @Schema(implementation = BidResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid bid request or bid amount too low"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<BidResponse> placeBid(
            @Parameter(description = "Bid details including bidder name, amount, and item ID", required = true)
            @Valid @RequestBody BidRequest bidRequest) {
        try {
            BidResponse response = bidService.placeBid(bidRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Get all bids for an item", description = "Retrieve all bids placed on a specific auction item, ordered by amount (highest first)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bids",
                    content = @Content(schema = @Schema(implementation = BidResponse.class)))
    })
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<BidResponse>> getBidsByItemId(
            @Parameter(description = "ID of the auction item", required = true)
            @PathVariable Long itemId) {
        List<BidResponse> bids = bidService.getBidsByItemId(itemId);
        return ResponseEntity.ok(bids);
    }
    
    @Operation(summary = "Get winning bid for an item", description = "Retrieve the current winning bid for a specific auction item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved winning bid",
                    content = @Content(schema = @Schema(implementation = BidResponse.class))),
            @ApiResponse(responseCode = "404", description = "No winning bid found for this item")
    })
    @GetMapping("/item/{itemId}/winning")
    public ResponseEntity<BidResponse> getWinningBid(
            @Parameter(description = "ID of the auction item", required = true)
            @PathVariable Long itemId) {
        BidResponse winningBid = bidService.getWinningBid(itemId);
        if (winningBid != null) {
            return ResponseEntity.ok(winningBid);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Get bid count for an item", description = "Get the total number of bids placed on a specific auction item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bid count")
    })
    @GetMapping("/item/{itemId}/count")
    public ResponseEntity<Long> getBidCount(
            @Parameter(description = "ID of the auction item", required = true)
            @PathVariable Long itemId) {
        Long count = bidService.getBidCount(itemId);
        return ResponseEntity.ok(count);
    }
    
    @Operation(summary = "Get top bids for an item", description = "Retrieve the top N bids for a specific auction item, ordered by amount (highest first)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top bids",
                    content = @Content(schema = @Schema(implementation = BidResponse.class)))
    })
    @GetMapping("/item/{itemId}/top")
    public ResponseEntity<List<BidResponse>> getTopBids(
            @Parameter(description = "ID of the auction item", required = true)
            @PathVariable Long itemId,
            @Parameter(description = "Maximum number of bids to return (default: 10)")
            @RequestParam(defaultValue = "10") int limit) {
        List<BidResponse> topBids = bidService.getTopBids(itemId, limit);
        return ResponseEntity.ok(topBids);
    }
}
