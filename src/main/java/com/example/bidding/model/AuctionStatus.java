// Package containing JPA entity models and related enums
package com.example.bidding.model;

// Enumeration of possible auction lifecycle states
public enum AuctionStatus {
    // Auction is ongoing and accepts bids
    ACTIVE,
    // Auction has ended; bidding is closed
    ENDED,
    // Auction was cancelled before completion
    CANCELLED
}
