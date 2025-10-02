package com.example.bidding.controller;

import com.example.bidding.entity.User;
import com.example.bidding.entity.User.Role;
import com.example.bidding.repository.UserRepository;
import com.example.bidding.repository.ItemRepository;
import com.example.bidding.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private BidRepository bidRepository;

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(u -> ResponseEntity.ok(convertToDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update user role
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        try {
            Role newRole = Role.valueOf(request.getRole().toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
            return ResponseEntity.ok(convertToDTO(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Toggle user enabled/disabled status
     */
    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<UserDTO> toggleUserStatus(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(convertToDTO(user));
    }

    /**
     * Get platform statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<PlatformStats> getPlatformStats() {
        PlatformStats stats = new PlatformStats();
        
        // User statistics
        List<User> allUsers = userRepository.findAll();
        stats.setTotalUsers(allUsers.size());
        stats.setBuyerCount((int) allUsers.stream().filter(u -> u.getRole() == Role.BUYER).count());
        stats.setSellerCount((int) allUsers.stream().filter(u -> u.getRole() == Role.SELLER).count());
        stats.setAdminCount((int) allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count());
        stats.setActiveUsers((int) allUsers.stream().filter(User::isEnabled).count());
        stats.setDisabledUsers((int) allUsers.stream().filter(u -> !u.isEnabled()).count());
        
        // Auction statistics
        stats.setTotalAuctions(itemRepository.count());
        stats.setActiveAuctions((int) itemRepository.count()); // Simplified - would need custom query for active only
        
        // Bid statistics
        stats.setTotalBids(bidRepository.count());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Get user activity summary
     */
    @GetMapping("/users/{id}/activity")
    public ResponseEntity<UserActivity> getUserActivity(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        UserActivity activity = new UserActivity();
        activity.setUserId(user.getId());
        activity.setUsername(user.getUsername());
        
        // Count bids placed by this user
        long bidCount = bidRepository.findAll().stream()
                .filter(bid -> bid.getBidderName().equals(user.getFirstName() + " " + user.getLastName()))
                .count();
        activity.setBidsPlaced(bidCount);
        
        return ResponseEntity.ok(activity);
    }

    // Helper method to convert User to DTO (without password)
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRole(user.getRole().name());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    // DTOs
    public static class UserDTO {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private boolean enabled;
        private java.time.LocalDateTime createdAt;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class RoleUpdateRequest {
        private String role;
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class PlatformStats {
        private int totalUsers;
        private int buyerCount;
        private int sellerCount;
        private int adminCount;
        private int activeUsers;
        private int disabledUsers;
        private long totalAuctions;
        private long activeAuctions;
        private long totalBids;

        // Getters and Setters
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        public int getBuyerCount() { return buyerCount; }
        public void setBuyerCount(int buyerCount) { this.buyerCount = buyerCount; }
        public int getSellerCount() { return sellerCount; }
        public void setSellerCount(int sellerCount) { this.sellerCount = sellerCount; }
        public int getAdminCount() { return adminCount; }
        public void setAdminCount(int adminCount) { this.adminCount = adminCount; }
        public int getActiveUsers() { return activeUsers; }
        public void setActiveUsers(int activeUsers) { this.activeUsers = activeUsers; }
        public int getDisabledUsers() { return disabledUsers; }
        public void setDisabledUsers(int disabledUsers) { this.disabledUsers = disabledUsers; }
        public long getTotalAuctions() { return totalAuctions; }
        public void setTotalAuctions(long totalAuctions) { this.totalAuctions = totalAuctions; }
        public long getActiveAuctions() { return activeAuctions; }
        public void setActiveAuctions(long activeAuctions) { this.activeAuctions = activeAuctions; }
        public long getTotalBids() { return totalBids; }
        public void setTotalBids(long totalBids) { this.totalBids = totalBids; }
    }

    public static class UserActivity {
        private Long userId;
        private String username;
        private long bidsPlaced;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public long getBidsPlaced() { return bidsPlaced; }
        public void setBidsPlaced(long bidsPlaced) { this.bidsPlaced = bidsPlaced; }
    }
}

