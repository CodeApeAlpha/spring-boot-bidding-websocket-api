package com.example.bidding.controller;

import com.example.bidding.model.Item;
import com.example.bidding.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@Tag(name = "Item Management", description = "APIs for managing auction items")
public class ItemController {
    
    @Autowired
    private ItemService itemService;
    
    @Operation(summary = "Create a new auction item", description = "Create a new auction item with specified details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item created successfully",
                    content = @Content(schema = @Schema(implementation = Item.class))),
            @ApiResponse(responseCode = "400", description = "Invalid item data")
    })
    @PostMapping
    public ResponseEntity<Item> createItem(
            @Parameter(description = "Auction item details", required = true)
            @RequestBody Item item) {
        Item createdItem = itemService.createItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }
    
    @Operation(summary = "Get all auction items", description = "Retrieve all auction items regardless of status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all items",
                    content = @Content(schema = @Schema(implementation = Item.class)))
    })
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }
    
    @Operation(summary = "Get active auction items", description = "Retrieve only auction items that are currently active")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active items",
                    content = @Content(schema = @Schema(implementation = Item.class)))
    })
    @GetMapping("/active")
    public ResponseEntity<List<Item>> getActiveItems() {
        List<Item> activeItems = itemService.getActiveItems();
        return ResponseEntity.ok(activeItems);
    }
    
    @Operation(summary = "Get item by ID", description = "Retrieve a specific auction item by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved item",
                    content = @Content(schema = @Schema(implementation = Item.class))),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(
            @Parameter(description = "ID of the auction item", required = true)
            @PathVariable Long id) {
        Optional<Item> item = itemService.getItemById(id);
        return item.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Update an auction item", description = "Update an existing auction item with new details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully",
                    content = @Content(schema = @Schema(implementation = Item.class))),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "400", description = "Invalid item data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @Parameter(description = "ID of the auction item to update", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated item details", required = true)
            @RequestBody Item item) {
        Optional<Item> existingItem = itemService.getItemById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            Item updatedItem = itemService.updateItem(item);
            return ResponseEntity.ok(updatedItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Delete an auction item", description = "Delete an auction item by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "ID of the auction item to delete", required = true)
            @PathVariable Long id) {
        Optional<Item> item = itemService.getItemById(id);
        if (item.isPresent()) {
            itemService.deleteItem(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
