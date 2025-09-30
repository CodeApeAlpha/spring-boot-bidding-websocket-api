// Package declaration for controller classes
package com.example.bidding.controller;

// Import the Item entity model
import com.example.bidding.model.Item;
// Import the service that encapsulates item-related business logic
import com.example.bidding.service.ItemService;
// Swagger annotation for operation metadata in API docs
import io.swagger.v3.oas.annotations.Operation;
// Swagger annotation for documenting method parameters
import io.swagger.v3.oas.annotations.Parameter;
// Swagger annotation for response content
import io.swagger.v3.oas.annotations.media.Content;
// Swagger schema reference to annotate models
import io.swagger.v3.oas.annotations.media.Schema;
// Swagger annotation for a single API response
import io.swagger.v3.oas.annotations.responses.ApiResponse;
// Swagger annotation for grouping multiple API responses
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Swagger annotation to tag the controller in the docs
import io.swagger.v3.oas.annotations.tags.Tag;
// Spring annotation for dependency injection
import org.springframework.beans.factory.annotation.Autowired;
// HTTP status enumeration
import org.springframework.http.HttpStatus;
// Wrapper for HTTP responses with status and body
import org.springframework.http.ResponseEntity;
// Spring Web annotations for REST endpoints
import org.springframework.web.bind.annotation.*;

// Java util list for collections
import java.util.List;
// Optional type for possibly missing items
import java.util.Optional;

// Marks this class as a REST controller
@RestController
// Base path for item-related endpoints
@RequestMapping("/api/items")
// Allow cross-origin requests from any origin (for demo/testing)
@CrossOrigin(origins = "*")
// Tag used in Swagger for grouping
@Tag(name = "Item Management", description = "APIs for managing auction items")
// Controller exposing CRUD endpoints for auction items
public class ItemController {
    
    // Inject the item service implementation
    @Autowired
    private ItemService itemService;
    
    // Document endpoint to create a new item
    @Operation(summary = "Create a new auction item", description = "Create a new auction item with specified details")
    // Possible outcomes for item creation
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Item created successfully",
                    content = @Content(schema = @Schema(implementation = Item.class))),
            @ApiResponse(responseCode = "400", description = "Invalid item data")
    })
    // HTTP POST to create an item
    @PostMapping
    // Create an item and return 201 Created
    public ResponseEntity<Item> createItem(
            @Parameter(description = "Auction item details", required = true)
            // Accept item details in request body
            @RequestBody Item item) {
        // Delegate to service layer to persist item
        Item createdItem = itemService.createItem(item);
        // Respond with the created item
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }
    
    // Document endpoint to fetch all items
    @Operation(summary = "Get all auction items", description = "Retrieve all auction items regardless of status")
    // 200 OK with list of items
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all items",
                    content = @Content(schema = @Schema(implementation = Item.class)))
    })
    // HTTP GET to list all items
    @GetMapping
    // Return all items currently stored
    public ResponseEntity<List<Item>> getAllItems() {
        // Delegate to service to fetch data
        List<Item> items = itemService.getAllItems();
        // Return 200 OK with payload
        return ResponseEntity.ok(items);
    }
    
    // Document endpoint to filter active items
    @Operation(summary = "Get active auction items", description = "Retrieve only auction items that are currently active")
    // Standard success response
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved active items",
                    content = @Content(schema = @Schema(implementation = Item.class)))
    })
    // HTTP GET to list active items
    @GetMapping("/active")
    // Returns only items with ACTIVE status
    public ResponseEntity<List<Item>> getActiveItems() {
        // Service filters active items
        List<Item> activeItems = itemService.getActiveItems();
        // Return 200 OK with payload
        return ResponseEntity.ok(activeItems);
    }
    
    // Document endpoint to fetch an item by its ID
    @Operation(summary = "Get item by ID", description = "Retrieve a specific auction item by its ID")
    // Success and not-found responses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved item",
                    content = @Content(schema = @Schema(implementation = Item.class))),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    // HTTP GET to fetch single item
    @GetMapping("/{id}")
    // Return the item if present, otherwise 404
    public ResponseEntity<Item> getItemById(
            @Parameter(description = "ID of the auction item", required = true)
            // Bind path variable to method parameter
            @PathVariable Long id) {
        // Delegate to service to find item
        Optional<Item> item = itemService.getItemById(id);
        // Map present item to 200 OK, else return 404
        return item.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    // Document endpoint to update an existing item
    @Operation(summary = "Update an auction item", description = "Update an existing auction item with new details")
    // Success, not-found, and bad-request responses
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item updated successfully",
                    content = @Content(schema = @Schema(implementation = Item.class))),
            @ApiResponse(responseCode = "404", description = "Item not found"),
            @ApiResponse(responseCode = "400", description = "Invalid item data")
    })
    // HTTP PUT to update an item by ID
    @PutMapping("/{id}")
    // Update the item if it exists
    public ResponseEntity<Item> updateItem(
            @Parameter(description = "ID of the auction item to update", required = true)
            // ID of the item to update
            @PathVariable Long id,
            @Parameter(description = "Updated item details", required = true)
            // New values for the item
            @RequestBody Item item) {
        // Ensure the item exists before updating
        Optional<Item> existingItem = itemService.getItemById(id);
        if (existingItem.isPresent()) {
            // Preserve the ID and delegate update to service
            item.setId(id);
            Item updatedItem = itemService.updateItem(item);
            return ResponseEntity.ok(updatedItem);
        } else {
            // Return 404 if the item does not exist
            return ResponseEntity.notFound().build();
        }
    }
    
    // Document endpoint to delete an item
    @Operation(summary = "Delete an auction item", description = "Delete an auction item by its ID")
    // 204 for success, 404 if not found
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    // HTTP DELETE to remove an item by ID
    @DeleteMapping("/{id}")
    // Delete the item if present
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "ID of the auction item to delete", required = true)
            // Bind path variable to ID
            @PathVariable Long id) {
        // Fetch item to verify existence
        Optional<Item> item = itemService.getItemById(id);
        if (item.isPresent()) {
            // Delegate deletion to service and return 204
            itemService.deleteItem(id);
            return ResponseEntity.noContent().build();
        } else {
            // Return 404 if the item does not exist
            return ResponseEntity.notFound().build();
        }
    }
}
