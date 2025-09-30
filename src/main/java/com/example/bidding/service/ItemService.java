// Package containing service layer classes
package com.example.bidding.service;

// Enum representing auction status values
import com.example.bidding.model.AuctionStatus;
// Entity representing an auction item
import com.example.bidding.model.Item;
// Repository for item persistence operations
import com.example.bidding.repository.JdbcItemRepository;
// Spring dependency injection
import org.springframework.beans.factory.annotation.Autowired;
// Scheduler to run periodic tasks
import org.springframework.scheduling.annotation.Scheduled;
// Marks this class as a Spring service component
import org.springframework.stereotype.Service;
// Transactional annotation to wrap public methods in DB transactions
import org.springframework.transaction.annotation.Transactional;

// Timestamp utilities
import java.time.LocalDateTime;
// Collections utilities
import java.util.List;
// Optional wrapper for possibly missing entities
import java.util.Optional;

// Service component managed by Spring
@Service
// Enable transactional behavior for public methods
@Transactional
public class ItemService {
    
    // Inject item repository
    @Autowired
    private JdbcItemRepository itemRepository;
    
    // Create and persist a new item entity
    public Item createItem(Item item) {
        return itemRepository.save(item);
    }
    
    // Retrieve all items
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    // Retrieve only items with ACTIVE status
    public List<Item> getActiveItems() {
        return itemRepository.findByStatus(AuctionStatus.ACTIVE);
    }
    
    // Find a single item by ID
    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }
    
    // Update an existing item
    public Item updateItem(Item item) {
        return itemRepository.save(item);
    }
    
    // Delete an item by ID
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
    
    // Scheduled method to end expired auctions
    @Scheduled(fixedRate = 60000) // Run every minute
    public void endExpiredAuctions() {
        List<Item> expiredItems = itemRepository.findExpiredItems(
            AuctionStatus.ACTIVE, 
            LocalDateTime.now()
        );
        
        for (Item item : expiredItems) {
            item.setStatus(AuctionStatus.ENDED);
            itemRepository.save(item);
        }
    }
    
    // Retrieve items filtered by status
    public List<Item> getItemsByStatus(AuctionStatus status) {
        return itemRepository.findByStatus(status);
    }
}
