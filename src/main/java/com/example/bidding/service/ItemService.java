package com.example.bidding.service;

import com.example.bidding.model.AuctionStatus;
import com.example.bidding.model.Item;
import com.example.bidding.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ItemService {
    
    @Autowired
    private ItemRepository itemRepository;
    
    public Item createItem(Item item) {
        return itemRepository.save(item);
    }
    
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    
    public List<Item> getActiveItems() {
        return itemRepository.findByStatus(AuctionStatus.ACTIVE);
    }
    
    public Optional<Item> getItemById(Long id) {
        return itemRepository.findById(id);
    }
    
    public Item updateItem(Item item) {
        return itemRepository.save(item);
    }
    
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
    
    public List<Item> getItemsByStatus(AuctionStatus status) {
        return itemRepository.findByStatus(status);
    }
}
