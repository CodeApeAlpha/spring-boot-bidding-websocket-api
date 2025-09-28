-- SQL Server Setup Script for Bidding WebSocket API
-- Run this script in SQL Server Management Studio or Azure Data Studio

-- Create the database if it doesn't exist
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'SampleDB')
BEGIN
    CREATE DATABASE SampleDB;
    PRINT 'Database SampleDB created successfully.';
END
ELSE
BEGIN
    PRINT 'Database SampleDB already exists.';
END

-- Use the SampleDB database
USE SampleDB;

-- Create the items table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='items' AND xtype='U')
BEGIN
    CREATE TABLE items (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(255) NOT NULL,
        description NVARCHAR(MAX),
        starting_price DECIMAL(10,2) NOT NULL,
        current_highest_bid DECIMAL(10,2),
        start_time DATETIME2 NOT NULL,
        end_time DATETIME2 NOT NULL,
        status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
    );
    PRINT 'Table items created successfully.';
END
ELSE
BEGIN
    PRINT 'Table items already exists.';
END

-- Create the bids table
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='bids' AND xtype='U')
BEGIN
    CREATE TABLE bids (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        bidder_name NVARCHAR(255) NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        item_id BIGINT NOT NULL,
        timestamp DATETIME2 NOT NULL,
        is_winning BIT NOT NULL DEFAULT 0,
        FOREIGN KEY (item_id) REFERENCES items(id)
    );
    PRINT 'Table bids created successfully.';
END
ELSE
BEGIN
    PRINT 'Table bids already exists.';
END

-- Insert sample data
IF NOT EXISTS (SELECT * FROM items WHERE name = 'Vintage Guitar')
BEGIN
    INSERT INTO items (name, description, starting_price, current_highest_bid, start_time, end_time, status)
    VALUES 
        ('Vintage Guitar', 'Beautiful vintage acoustic guitar from the 1960s', 500.00, 500.00, GETDATE(), DATEADD(hour, 24, GETDATE()), 'ACTIVE'),
        ('Antique Watch', 'Rare Swiss pocket watch from 1890', 1000.00, 1000.00, GETDATE(), DATEADD(hour, 48, GETDATE()), 'ACTIVE'),
        ('Art Painting', 'Original oil painting by local artist', 200.00, 200.00, GETDATE(), DATEADD(hour, 12, GETDATE()), 'ACTIVE');
    
    PRINT 'Sample data inserted successfully.';
END
ELSE
BEGIN
    PRINT 'Sample data already exists.';
END

-- Verify the setup
SELECT 'Database Setup Complete!' as Status;
SELECT COUNT(*) as ItemCount FROM items;
SELECT COUNT(*) as BidCount FROM bids;
