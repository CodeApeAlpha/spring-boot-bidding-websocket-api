package com.example.bidding.repository;

import com.example.bidding.entity.AuctionStatus;
import com.example.bidding.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    List<Item> findByStatus(AuctionStatus status);
    
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.endTime > :now ORDER BY i.endTime ASC")
    List<Item> findActiveItems(@Param("status") AuctionStatus status, @Param("now") LocalDateTime now);
    
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.endTime <= :now ORDER BY i.endTime DESC")
    List<Item> findExpiredItems(@Param("status") AuctionStatus status, @Param("now") LocalDateTime now);
    
    @Query("SELECT i FROM Item i WHERE i.id = :itemId AND i.status = :status")
    Optional<Item> findActiveItemById(@Param("itemId") Long itemId, @Param("status") AuctionStatus status);
    
    @Query("SELECT COUNT(i) FROM Item i WHERE i.status = :status")
    long countByStatus(@Param("status") AuctionStatus status);
}
