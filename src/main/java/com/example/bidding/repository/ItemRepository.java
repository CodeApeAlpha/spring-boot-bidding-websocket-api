package com.example.bidding.repository;

import com.example.bidding.model.AuctionStatus;
import com.example.bidding.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    List<Item> findByStatus(AuctionStatus status);
    
    List<Item> findByStatusAndEndTimeAfter(AuctionStatus status, LocalDateTime endTime);
    
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.endTime > :currentTime ORDER BY i.endTime ASC")
    List<Item> findActiveItemsOrderByEndTime(@Param("status") AuctionStatus status, 
                                           @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.endTime <= :currentTime")
    List<Item> findExpiredItems(@Param("status") AuctionStatus status, 
                               @Param("currentTime") LocalDateTime currentTime);
}
