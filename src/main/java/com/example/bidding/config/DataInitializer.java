package com.example.bidding.config;

import com.example.bidding.model.Item;
import com.example.bidding.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Override
    public void run(String... args) throws Exception {
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
        
        System.out.println("Sample auction items created successfully!");
    }
}
