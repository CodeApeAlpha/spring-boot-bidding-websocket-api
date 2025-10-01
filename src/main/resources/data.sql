-- Sample data for testing

-- NOTE: Test users should be created via registration endpoint (/api/auth/register)
-- This ensures passwords are properly BCrypt hashed
-- 
-- Recommended test users to create via UI or API:
-- 1. buyer1 / password123 (BUYER role)
-- 2. seller1 / password123 (SELLER role)
-- 3. admin1 / password123 (ADMIN role)

-- Sample Auction Items
INSERT INTO items (name, description, starting_price, current_highest_bid, start_time, end_time, status) VALUES
('Vintage Guitar', 'Beautiful 1960s Fender Stratocaster', 500.00, 500.00, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 'ACTIVE'),
('Antique Watch', 'Rare Rolex Submariner from 1970s', 2000.00, 2000.00, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 'ACTIVE'),
('Art Painting', 'Original oil painting by local artist', 150.00, 150.00, NOW(), DATE_ADD(NOW(), INTERVAL 3 DAY), 'ACTIVE');
