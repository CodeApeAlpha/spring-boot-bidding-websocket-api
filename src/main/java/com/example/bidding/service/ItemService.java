package com.example.bidding.service;

import com.example.bidding.dto.response.ItemResponse;
import com.example.bidding.entity.AuctionStatus;
import com.example.bidding.entity.Item;
import com.example.bidding.exception.ResourceNotFoundException;
import com.example.bidding.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ItemService {
    
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    
    @Autowired
    private ItemRepository itemRepository;
    
    public Item createItem(Item item) {
        logger.info("Creating new item: {}", item.getName());
        return itemRepository.save(item);
    }
    
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems() {
        logger.debug("Retrieving all items");
        return itemRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<ItemResponse> getActiveItems() {
        logger.debug("Retrieving active items");
        return itemRepository.findActiveItems(AuctionStatus.ACTIVE, LocalDateTime.now()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        logger.debug("Retrieving item by ID: {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return convertToResponse(item);
    }
    
    public ItemResponse updateItem(Long id, Item item) {
        logger.info("Updating item with ID: {}", id);
        Item existingItem = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        
        existingItem.setName(item.getName());
        existingItem.setDescription(item.getDescription());
        existingItem.setStartingPrice(item.getStartingPrice());
        existingItem.setEndTime(item.getEndTime());
        
        Item savedItem = itemRepository.save(existingItem);
        return convertToResponse(savedItem);
    }
    
    public void deleteItem(Long id) {
        logger.info("Deleting item with ID: {}", id);
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }
    
    @Scheduled(fixedRate = 60000) // Run every minute
    public void endExpiredAuctions() {
        logger.debug("Checking for expired auctions");
        List<Item> expiredItems = itemRepository.findExpiredItems(
                AuctionStatus.ACTIVE, 
                LocalDateTime.now()
        );
        
        if (!expiredItems.isEmpty()) {
            logger.info("Found {} expired items, updating status", expiredItems.size());
            expiredItems.forEach(item -> {
                item.setStatus(AuctionStatus.EXPIRED);
                itemRepository.save(item);
            });
        }
    }
    
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByStatus(AuctionStatus status) {
        logger.debug("Retrieving items by status: {}", status);
        return itemRepository.findByStatus(status).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private ItemResponse convertToResponse(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getStartingPrice(),
                item.getCurrentHighestBid(),
                item.getStartTime(),
                item.getEndTime(),
                item.getStatus()
        );
    }
}