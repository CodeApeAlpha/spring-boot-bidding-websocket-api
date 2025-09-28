package com.example.bidding.controller;

import com.example.bidding.dto.BidRequest;
import com.example.bidding.dto.BidResponse;
import com.example.bidding.service.BidService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "*")
public class BidController {
    
    @Autowired
    private BidService bidService;
    
    @PostMapping
    public ResponseEntity<BidResponse> placeBid(@Valid @RequestBody BidRequest bidRequest) {
        try {
            BidResponse response = bidService.placeBid(bidRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/item/{itemId}")
    public ResponseEntity<List<BidResponse>> getBidsByItemId(@PathVariable Long itemId) {
        List<BidResponse> bids = bidService.getBidsByItemId(itemId);
        return ResponseEntity.ok(bids);
    }
    
    @GetMapping("/item/{itemId}/winning")
    public ResponseEntity<BidResponse> getWinningBid(@PathVariable Long itemId) {
        BidResponse winningBid = bidService.getWinningBid(itemId);
        if (winningBid != null) {
            return ResponseEntity.ok(winningBid);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/item/{itemId}/count")
    public ResponseEntity<Long> getBidCount(@PathVariable Long itemId) {
        Long count = bidService.getBidCount(itemId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/item/{itemId}/top")
    public ResponseEntity<List<BidResponse>> getTopBids(
            @PathVariable Long itemId,
            @RequestParam(defaultValue = "10") int limit) {
        List<BidResponse> topBids = bidService.getTopBids(itemId, limit);
        return ResponseEntity.ok(topBids);
    }
}
