-- Sample data for testing
INSERT INTO items (name, description, starting_price, current_highest_bid, start_time, end_time, status) VALUES
('Vintage Guitar', 'Beautiful 1960s Fender Stratocaster', 500.00, 500.00, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'ACTIVE'),
('Antique Watch', 'Rare Rolex Submariner from 1970s', 2000.00, 2000.00, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 'ACTIVE'),
('Art Painting', 'Original oil painting by local artist', 150.00, 150.00, NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 'ACTIVE');
