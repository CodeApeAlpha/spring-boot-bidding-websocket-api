package com.example.bidding.repository;

import com.example.bidding.model.Bid;
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
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcBidRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Bid> bidRowMapper = new RowMapper<Bid>() {
        @Override
        public Bid mapRow(ResultSet rs, int rowNum) throws SQLException {
            Bid bid = new Bid();
            bid.setId(rs.getLong("id"));
            bid.setBidderName(rs.getString("bidder_name"));
            bid.setAmount(rs.getBigDecimal("amount"));
            bid.setItemId(rs.getLong("item_id"));
            bid.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            bid.setIsWinning(rs.getBoolean("is_winning"));
            return bid;
        }
    };

    public Bid save(Bid bid) {
        if (bid.getId() == null) {
            return insert(bid);
        } else {
            return update(bid);
        }
    }

    private Bid insert(Bid bid) {
        String sql = "INSERT INTO bids (bidder_name, amount, item_id, timestamp, is_winning) VALUES (?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, bid.getBidderName());
            ps.setBigDecimal(2, bid.getAmount());
            ps.setLong(3, bid.getItemId());
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(bid.getTimestamp()));
            ps.setBoolean(5, bid.getIsWinning());
            return ps;
        }, keyHolder);
        
        Long generatedId = keyHolder.getKey().longValue();
        if (generatedId != null) {
            bid.setId(generatedId);
        }
        return bid;
    }

    private Bid update(Bid bid) {
        String sql = "UPDATE bids SET bidder_name = ?, amount = ?, item_id = ?, timestamp = ?, is_winning = ? WHERE id = ?";
        
        jdbcTemplate.update(sql,
                bid.getBidderName(),
                bid.getAmount(),
                bid.getItemId(),
                java.sql.Timestamp.valueOf(bid.getTimestamp()),
                bid.getIsWinning(),
                bid.getId());
        
        return bid;
    }

    public Optional<Bid> findById(Long id) {
        String sql = "SELECT * FROM bids WHERE id = ?";
        List<Bid> bids = jdbcTemplate.query(sql, bidRowMapper, id);
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.get(0));
    }

    public List<Bid> findAll() {
        String sql = "SELECT * FROM bids ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, bidRowMapper);
    }

    public List<Bid> findByItemIdOrderByAmountDesc(Long itemId) {
        String sql = "SELECT * FROM bids WHERE item_id = ? ORDER BY amount DESC";
        return jdbcTemplate.query(sql, bidRowMapper, itemId);
    }

    public List<Bid> findByItemIdOrderByTimestampDesc(Long itemId) {
        String sql = "SELECT * FROM bids WHERE item_id = ? ORDER BY timestamp DESC";
        return jdbcTemplate.query(sql, bidRowMapper, itemId);
    }

    public Optional<Bid> findWinningBidByItemId(Long itemId) {
        String sql = "SELECT * FROM bids WHERE item_id = ? AND is_winning = true";
        List<Bid> bids = jdbcTemplate.query(sql, bidRowMapper, itemId);
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.get(0));
    }

    public List<Bid> findTopBidsByItemId(Long itemId) {
        String sql = "SELECT * FROM bids WHERE item_id = ? ORDER BY amount DESC";
        return jdbcTemplate.query(sql, bidRowMapper, itemId);
    }

    public Long countBidsByItemId(Long itemId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE item_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, itemId);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM bids WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void updateWinningBids(Long itemId) {
        // First, set all bids for this item as not winning
        String resetSql = "UPDATE bids SET is_winning = false WHERE item_id = ?";
        jdbcTemplate.update(resetSql, itemId);
        
        // Then, set the highest bid as winning
        String setWinningSql = "UPDATE bids SET is_winning = true WHERE item_id = ? AND amount = (SELECT MAX(amount) FROM bids WHERE item_id = ?)";
        jdbcTemplate.update(setWinningSql, itemId, itemId);
    }
}
