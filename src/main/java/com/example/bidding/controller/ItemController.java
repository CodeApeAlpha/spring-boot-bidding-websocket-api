package com.example.bidding.controller;

import com.example.bidding.dto.response.ItemResponse;
import com.example.bidding.entity.AuctionStatus;
import com.example.bidding.entity.Item;
import com.example.bidding.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@Tag(name = "Item Management", description = "APIs for managing auction items")
public class ItemController {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    
    @Autowired
    private ItemService itemService;
    
    @GetMapping
    @Operation(summary = "Get all items", description = "Retrieve all auction items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Items retrieved successfully",
                content = @Content(schema = @Schema(implementation = ItemResponse.class)))
    })
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        logger.debug("Retrieving all items");
        List<ItemResponse> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/active")
    @Operation(summary = "Get active items", description = "Retrieve only active auction items")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active items retrieved successfully",
                content = @Content(schema = @Schema(implementation = ItemResponse.class)))
    })
    public ResponseEntity<List<ItemResponse>> getActiveItems() {
        logger.debug("Retrieving active items");
        List<ItemResponse> items = itemService.getActiveItems();
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieve a specific auction item by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item retrieved successfully",
                content = @Content(schema = @Schema(implementation = ItemResponse.class))),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<ItemResponse> getItemById(
            @Parameter(description = "ID of the item to retrieve") 
            @PathVariable Long id) {
        
        logger.debug("Retrieving item with ID: {}", id);
        ItemResponse item = itemService.getItemById(id);
        return ResponseEntity.ok(item);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get items by status", description = "Retrieve items filtered by auction status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Items retrieved successfully",
                content = @Content(schema = @Schema(implementation = ItemResponse.class)))
    })
    public ResponseEntity<List<ItemResponse>> getItemsByStatus(
            @Parameter(description = "Auction status to filter by") 
            @PathVariable AuctionStatus status) {
        
        logger.debug("Retrieving items with status: {}", status);
        List<ItemResponse> items = itemService.getItemsByStatus(status);
        return ResponseEntity.ok(items);
    }
    
    @PostMapping
    @Operation(summary = "Create new item", description = "Create a new auction item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item created successfully",
                content = @Content(schema = @Schema(implementation = ItemResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid item data")
    })
    public ResponseEntity<ItemResponse> createItem(@RequestBody Item item) {
        logger.info("Creating new item: {}", item.getName());
        Item createdItem = itemService.createItem(item);
        ItemResponse response = new ItemResponse(
                createdItem.getId(),
                createdItem.getName(),
                createdItem.getDescription(),
                createdItem.getStartingPrice(),
                createdItem.getCurrentHighestBid(),
                createdItem.getStartTime(),
                createdItem.getEndTime(),
                createdItem.getStatus()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update item", description = "Update an existing auction item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item updated successfully",
                content = @Content(schema = @Schema(implementation = ItemResponse.class))),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "400", description = "Invalid item data")
    })
    public ResponseEntity<ItemResponse> updateItem(
            @Parameter(description = "ID of the item to update") 
            @PathVariable Long id,
            @RequestBody Item item) {
        
        logger.info("Updating item with ID: {}", id);
        ItemResponse response = itemService.updateItem(id, item);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete item", description = "Delete an auction item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "ID of the item to delete") 
            @PathVariable Long id) {
        
        logger.info("Deleting item with ID: {}", id);
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}