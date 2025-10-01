package com.example.bidding.repository;

import com.example.bidding.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    
    @Query("SELECT b FROM Bid b WHERE b.item.id = :itemId ORDER BY b.amount DESC")
    List<Bid> findByItemIdOrderByAmountDesc(@Param("itemId") Long itemId);
    
    @Query("SELECT b FROM Bid b WHERE b.item.id = :itemId ORDER BY b.timestamp DESC")
    List<Bid> findByItemIdOrderByTimestampDesc(@Param("itemId") Long itemId);
    
    @Query("SELECT b FROM Bid b WHERE b.item.id = :itemId ORDER BY b.amount DESC")
    List<Bid> findHighestBidsByItemId(@Param("itemId") Long itemId);
    
    @Query("SELECT b FROM Bid b WHERE b.item.id = :itemId AND b.isWinning = true")
    Optional<Bid> findWinningBidByItemId(@Param("itemId") Long itemId);
    
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.item.id = :itemId")
    long countBidsByItemId(@Param("itemId") Long itemId);
    
    @Query("SELECT b FROM Bid b WHERE b.bidderName = :bidderName ORDER BY b.timestamp DESC")
    List<Bid> findByBidderNameOrderByTimestampDesc(@Param("bidderName") String bidderName);
}
