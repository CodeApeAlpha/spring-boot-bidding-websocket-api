package com.example.bidding.repository;

import com.example.bidding.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    
    List<Bid> findByItemIdOrderByAmountDesc(Long itemId);
    
    List<Bid> findByItemIdOrderByTimestampDesc(Long itemId);
    
    @Query("SELECT b FROM Bid b WHERE b.itemId = :itemId AND b.isWinning = true")
    Optional<Bid> findWinningBidByItemId(@Param("itemId") Long itemId);
    
    @Query("SELECT b FROM Bid b WHERE b.itemId = :itemId ORDER BY b.amount DESC")
    List<Bid> findTopBidsByItemId(@Param("itemId") Long itemId);
    
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.itemId = :itemId")
    Long countBidsByItemId(@Param("itemId") Long itemId);
}
