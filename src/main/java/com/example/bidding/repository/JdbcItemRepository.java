package com.example.bidding.repository;

import com.example.bidding.model.AuctionStatus;
import com.example.bidding.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcItemRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Item> itemRowMapper = new RowMapper<Item>() {
        @Override
        public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setName(rs.getString("name"));
            item.setDescription(rs.getString("description"));
            item.setStartingPrice(rs.getBigDecimal("starting_price"));
            item.setCurrentHighestBid(rs.getBigDecimal("current_highest_bid"));
            item.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            item.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            item.setStatus(AuctionStatus.valueOf(rs.getString("status")));
            return item;
        }
    };

    public Item save(Item item) {
        if (item.getId() == null) {
            return insert(item);
        } else {
            return update(item);
        }
    }

    private Item insert(Item item) {
        String sql = "INSERT INTO items (name, description, starting_price, current_highest_bid, start_time, end_time, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setBigDecimal(3, item.getStartingPrice());
            ps.setBigDecimal(4, item.getCurrentHighestBid());
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(item.getStartTime()));
            ps.setTimestamp(6, java.sql.Timestamp.valueOf(item.getEndTime()));
            ps.setString(7, item.getStatus().name());
            return ps;
        }, keyHolder);
        
        Long generatedId = keyHolder.getKey().longValue();
        if (generatedId != null) {
            item.setId(generatedId);
        }
        return item;
    }

    private Item update(Item item) {
        String sql = "UPDATE items SET name = ?, description = ?, starting_price = ?, current_highest_bid = ?, start_time = ?, end_time = ?, status = ? WHERE id = ?";
        
        jdbcTemplate.update(sql,
                item.getName(),
                item.getDescription(),
                item.getStartingPrice(),
                item.getCurrentHighestBid(),
                java.sql.Timestamp.valueOf(item.getStartTime()),
                java.sql.Timestamp.valueOf(item.getEndTime()),
                item.getStatus().name(),
                item.getId());
        
        return item;
    }

    public Optional<Item> findById(Long id) {
        String sql = "SELECT * FROM items WHERE id = ?";
        List<Item> items = jdbcTemplate.query(sql, itemRowMapper, id);
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    public List<Item> findAll() {
        String sql = "SELECT * FROM items ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, itemRowMapper);
    }

    public List<Item> findByStatus(AuctionStatus status) {
        String sql = "SELECT * FROM items WHERE status = ? ORDER BY start_time DESC";
        return jdbcTemplate.query(sql, itemRowMapper, status.name());
    }

    public List<Item> findByStatusAndEndTimeAfter(AuctionStatus status, LocalDateTime endTime) {
        String sql = "SELECT * FROM items WHERE status = ? AND end_time > ? ORDER BY end_time ASC";
        return jdbcTemplate.query(sql, itemRowMapper, status.name(), java.sql.Timestamp.valueOf(endTime));
    }

    public List<Item> findActiveItemsOrderByEndTime(AuctionStatus status, LocalDateTime currentTime) {
        String sql = "SELECT * FROM items WHERE status = ? AND end_time > ? ORDER BY end_time ASC";
        return jdbcTemplate.query(sql, itemRowMapper, status.name(), java.sql.Timestamp.valueOf(currentTime));
    }

    public List<Item> findExpiredItems(AuctionStatus status, LocalDateTime currentTime) {
        String sql = "SELECT * FROM items WHERE status = ? AND end_time <= ?";
        return jdbcTemplate.query(sql, itemRowMapper, status.name(), java.sql.Timestamp.valueOf(currentTime));
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM items WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
