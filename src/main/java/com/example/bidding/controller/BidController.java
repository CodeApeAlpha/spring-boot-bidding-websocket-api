// Package declaration for the controller classes in the bidding application
package com.example.bidding.controller;

// Import the request DTO for placing bids
import com.example.bidding.dto.BidRequest;
// Import the response DTO for returning bid details
import com.example.bidding.dto.BidResponse;
// Import the service that encapsulates bid business logic
import com.example.bidding.service.BidService;
// Swagger annotation for operation metadata
import io.swagger.v3.oas.annotations.Operation;
// Swagger annotation to describe parameters
import io.swagger.v3.oas.annotations.Parameter;
// Swagger annotation to describe response content
import io.swagger.v3.oas.annotations.media.Content;
// Swagger schema reference for DTOs
import io.swagger.v3.oas.annotations.media.Schema;
// Swagger annotation to document an API response
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Swagger annotation to group multiple API responses
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Swagger annotation to tag the controller in API docs
import io.swagger.v3.oas.annotations.tags.Tag;
// Bean validation annotation to validate incoming requests
import jakarta.validation.Valid;
// Spring annotation for dependency injection
import org.springframework.beans.factory.annotation.Autowired;
// HTTP status enumeration
import org.springframework.http.HttpStatus;
// Wrapper for HTTP responses with status and body
import org.springframework.http.ResponseEntity;
// Spring Web annotations for REST endpoints
import org.springframework.web.bind.annotation.*;

// Java util list for returning collections of responses
import java.util.List;

// Marks this class as a REST controller
@RestController
// Base path for all bid-related endpoints
@RequestMapping("/api/bids")
// Allow cross-origin requests from any origin (for demo/testing)
@CrossOrigin(origins = "*")
// Grouped under the "Bid Management" tag in Swagger
@Tag(name = "Bid Management", description = "APIs for managing bids on auction items")
// Controller that exposes endpoints to place and retrieve bids
public class BidController {
    
    // Inject the bid service implementation
    @Autowired
    private BidService bidService;
    
    // Document the purpose of the endpoint for Swagger
    @Operation(summary = "Place a new bid", description = "Place a bid on an auction item. The bid must be higher than the current highest bid.")
    // Possible responses for this endpoint
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bid placed successfully",
                    content = @Content(schema = @Schema(implementation = BidResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid bid request or bid amount too low"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    // HTTP POST endpoint to create a new bid
    @PostMapping
    // Method that handles the bid placement and returns the created bid
    public ResponseEntity<BidResponse> placeBid(
            @Parameter(description = "Bid details including bidder name, amount, and item ID", required = true)
            // Validate the request body against constraints in BidRequest
            @Valid @RequestBody BidRequest bidRequest) {
        // Try placing the bid and handle expected errors
        try {
            // Delegate to the service to process the bid
            BidResponse response = bidService.placeBid(bidRequest);
            // Return 201 Created with the bid response
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Bad request when validation/business rule fails (e.g., too-low bid)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            // Catch-all for unexpected server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Document endpoint to list all bids for a given item
    @Operation(summary = "Get all bids for an item", description = "Retrieve all bids placed on a specific auction item, ordered by amount (highest first)")
    // Document the standard successful response
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bids",
                    content = @Content(schema = @Schema(implementation = BidResponse.class)))
    })
    // HTTP GET to fetch bids for a given item ID
    @GetMapping("/item/{itemId}")
    // Returns a list of bids for the provided itemId
    public ResponseEntity<List<BidResponse>> getBidsByItemId(
            @Parameter(description = "ID of the auction item", required = true)
            // Bind path variable "itemId" to method parameter
            @PathVariable Long itemId) {
        // Delegate to service to fetch bids
        List<BidResponse> bids = bidService.getBidsByItemId(itemId);
        // Return 200 OK with the list of bids
        return ResponseEntity.ok(bids);
    }
    
    // Document endpoint to get current winning bid
    @Operation(summary = "Get winning bid for an item", description = "Retrieve the current winning bid for a specific auction item")
    // Possible outcomes include 200 and 404
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved winning bid",
                    content = @Content(schema = @Schema(implementation = BidResponse.class))),
            @ApiResponse(responseCode = "404", description = "No winning bid found for this item")
    })
    // HTTP GET to fetch the winning bid for an item
    @GetMapping("/item/{itemId}/winning")
    // Returns winning bid if present, otherwise 404
    public ResponseEntity<BidResponse> getWinningBid(
            @Parameter(description = "ID of the auction item", required = true)
            // Bind itemId from path
            @PathVariable Long itemId) {
        // Ask service for the winning bid
        BidResponse winningBid = bidService.getWinningBid(itemId);
        // If a winning bid exists, return it
        if (winningBid != null) {
            return ResponseEntity.ok(winningBid);
        } else {
            // Otherwise respond with 404 Not Found
            return ResponseEntity.notFound().build();
        }
    }
    
    // Document endpoint to count bids for an item
    @Operation(summary = "Get bid count for an item", description = "Get the total number of bids placed on a specific auction item")
    // Success response documented for Swagger
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved bid count")
    })
    // HTTP GET to obtain the number of bids
    @GetMapping("/item/{itemId}/count")
    // Return the number of bids for the provided itemId
    public ResponseEntity<Long> getBidCount(
            @Parameter(description = "ID of the auction item", required = true)
            // Bind itemId from path
            @PathVariable Long itemId) {
        // Delegate to service to count bids
        Long count = bidService.getBidCount(itemId);
        // Return 200 OK with the count value
        return ResponseEntity.ok(count);
    }
    
    // Document endpoint to fetch top N bids for an item
    @Operation(summary = "Get top bids for an item", description = "Retrieve the top N bids for a specific auction item, ordered by amount (highest first)")
    // Success response documented for Swagger
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved top bids",
                    content = @Content(schema = @Schema(implementation = BidResponse.class)))
    })
    // HTTP GET to retrieve top bids with a limit
    @GetMapping("/item/{itemId}/top")
    // Returns a list of top bids for the item
    public ResponseEntity<List<BidResponse>> getTopBids(
            @Parameter(description = "ID of the auction item", required = true)
            // Bind itemId from path
            @PathVariable Long itemId,
            // Limit query parameter with Swagger description
            @Parameter(description = "Maximum number of bids to return (default: 10)")
            // Default limit is 10 bids if not provided
            @RequestParam(defaultValue = "10") int limit) {
        // Ask service for top bids given the limit
        List<BidResponse> topBids = bidService.getTopBids(itemId, limit);
        // Return 200 OK with the list of top bids
        return ResponseEntity.ok(topBids);
    }
    
    // Document endpoint to get all bids from all items
    @Operation(summary = "Get all bids", description = "Retrieve all bids from all auction items for the shared live feed")
    // Success response documented for Swagger
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all bids",
                    content = @Content(schema = @Schema(implementation = BidResponse.class)))
    })
    // HTTP GET to retrieve all bids
    @GetMapping
    // Returns a list of all bids
    public ResponseEntity<List<BidResponse>> getAllBids() {
        // Ask service for all bids
        List<BidResponse> allBids = bidService.getAllBids();
        // Return 200 OK with the list of all bids
        return ResponseEntity.ok(allBids);
    }
}
