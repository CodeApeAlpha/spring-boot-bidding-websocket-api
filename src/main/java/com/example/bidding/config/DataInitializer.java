package com.example.bidding.config;

import com.example.bidding.entity.Item;
import com.example.bidding.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing sample data...");
        
        // Check if items already exist
        if (itemRepository.count() > 0) {
            logger.info("Sample data already exists, skipping initialization");
            return;
        }
        
        // Create sample auction items
        Item item1 = new Item(
            "Vintage Guitar",
            "Beautiful vintage acoustic guitar from the 1960s",
            new BigDecimal("500.00"),
            LocalDateTime.now().plusHours(24)
        );
        
        Item item2 = new Item(
            "Antique Watch",
            "Rare Swiss pocket watch from 1890",
            new BigDecimal("1000.00"),
            LocalDateTime.now().plusHours(48)
        );
        
        Item item3 = new Item(
            "Art Painting",
            "Original oil painting by local artist",
            new BigDecimal("200.00"),
            LocalDateTime.now().plusHours(12)
        );
        
        itemRepository.save(item1);
        itemRepository.save(item2);
        itemRepository.save(item3);
        
        logger.info("Sample auction items created successfully!");
    }
}