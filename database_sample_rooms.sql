-- =============================================
-- Room Management - Sample Data & Test Queries
-- =============================================
-- Use these queries to populate and test the rooms table
-- =============================================

-- =============================================
-- STEP 1: Verify Prerequisites
-- =============================================

-- Check if ships table exists and has data
SELECT id, name, capacity, type 
FROM ships 
ORDER BY name;

-- Check if room_types table exists and has data
SELECT id, name 
FROM room_types 
ORDER BY name;

-- If room_types is empty, add default types:
INSERT INTO room_types (name) VALUES 
    ('Single'),
    ('Double'),
    ('Suite'),
    ('Deluxe'),
    ('Family'),
    ('Presidential')
ON CONFLICT (name) DO NOTHING;

-- =============================================
-- STEP 2: Insert Sample Rooms
-- =============================================

-- Replace 'your-ship-uuid-here' with actual ship IDs from your database
-- Example: Get ship IDs first
DO $$ 
DECLARE
    ship1_id UUID;
    ship2_id UUID;
BEGIN
    -- Get first two ships (modify as needed)
    SELECT id INTO ship1_id FROM ships ORDER BY name LIMIT 1;
    SELECT id INTO ship2_id FROM ships ORDER BY name LIMIT 1 OFFSET 1;
    
    -- Insert rooms for Ship 1
    INSERT INTO rooms (id, ship_id, name, type, price, is_available) VALUES
        (gen_random_uuid(), ship1_id, 'A101', 'Single', 150.00, true),
        (gen_random_uuid(), ship1_id, 'A102', 'Single', 150.00, true),
        (gen_random_uuid(), ship1_id, 'A103', 'Double', 250.00, true),
        (gen_random_uuid(), ship1_id, 'A104', 'Double', 250.00, true),
        (gen_random_uuid(), ship1_id, 'B201', 'Suite', 450.00, true),
        (gen_random_uuid(), ship1_id, 'B202', 'Suite', 450.00, true),
        (gen_random_uuid(), ship1_id, 'B203', 'Deluxe', 550.00, true),
        (gen_random_uuid(), ship1_id, 'C301', 'Family', 600.00, true),
        (gen_random_uuid(), ship1_id, 'C302', 'Presidential', 1000.00, true)
    ON CONFLICT DO NOTHING;
    
    -- Insert rooms for Ship 2 (if exists)
    IF ship2_id IS NOT NULL THEN
        INSERT INTO rooms (id, ship_id, name, type, price, is_available) VALUES
            (gen_random_uuid(), ship2_id, 'D101', 'Single', 140.00, true),
            (gen_random_uuid(), ship2_id, 'D102', 'Double', 230.00, true),
            (gen_random_uuid(), ship2_id, 'D103', 'Suite', 420.00, true),
            (gen_random_uuid(), ship2_id, 'E201', 'Deluxe', 520.00, true),
            (gen_random_uuid(), ship2_id, 'E202', 'Family', 580.00, true)
        ON CONFLICT DO NOTHING;
    END IF;
    
    RAISE NOTICE 'Sample rooms inserted successfully!';
END $$;

-- =============================================
-- STEP 3: Verify Room Data
-- =============================================

-- View all rooms with ship names
SELECT 
    r.id,
    s.name AS ship_name,
    r.name AS room_number,
    r.type,
    r.price,
    r.is_available,
    r.created_at
FROM rooms r
JOIN ships s ON r.ship_id = s.id
ORDER BY s.name, r.name;

-- Count rooms by ship
SELECT 
    s.name AS ship_name,
    s.capacity AS ship_capacity,
    COUNT(r.id) AS total_rooms,
    s.capacity - COUNT(r.id) AS rooms_available_to_add
FROM ships s
LEFT JOIN rooms r ON s.id = r.ship_id
GROUP BY s.id, s.name, s.capacity
ORDER BY s.name;

-- Count rooms by type
SELECT 
    type,
    COUNT(*) AS count,
    AVG(price) AS avg_price,
    MIN(price) AS min_price,
    MAX(price) AS max_price
FROM rooms
GROUP BY type
ORDER BY count DESC;

-- Check available rooms
SELECT 
    s.name AS ship_name,
    r.name AS room_number,
    r.type,
    r.price
FROM rooms r
JOIN ships s ON r.ship_id = s.id
WHERE r.is_available = true
ORDER BY s.name, r.price;

-- =============================================
-- STEP 4: Test CRUD Operations
-- =============================================

-- CREATE: Insert a new room
-- (Replace UUIDs with actual values)
/*
INSERT INTO rooms (ship_id, name, type, price, is_available) 
VALUES (
    'your-ship-uuid-here',
    'F301',
    'Suite',
    500.00,
    true
)
RETURNING *;
*/

-- READ: Get specific room
SELECT * FROM rooms WHERE name = 'A101';

-- UPDATE: Modify room price
/*
UPDATE rooms 
SET price = 175.00, updated_at = NOW()
WHERE name = 'A101'
RETURNING *;
*/

-- UPDATE: Mark room as unavailable
/*
UPDATE rooms 
SET is_available = false, updated_at = NOW()
WHERE name = 'B201'
RETURNING *;
*/

-- DELETE: Remove a room
/*
DELETE FROM rooms 
WHERE name = 'F301'
RETURNING *;
*/

-- =============================================
-- STEP 5: Search & Filter Queries (Like Android App)
-- =============================================

-- Search rooms by number or type (case-insensitive)
SELECT 
    r.id,
    s.name AS ship_name,
    r.name AS room_number,
    r.type,
    r.price,
    r.is_available
FROM rooms r
JOIN ships s ON r.ship_id = s.id
WHERE 
    LOWER(r.name) LIKE LOWER('%101%') 
    OR LOWER(r.type) LIKE LOWER('%single%')
ORDER BY s.name, r.name;

-- Filter rooms by ship and availability
SELECT 
    r.name AS room_number,
    r.type,
    r.price
FROM rooms r
WHERE 
    r.ship_id = 'your-ship-uuid-here'
    AND r.is_available = true
ORDER BY r.price;

-- Get rooms in price range
SELECT 
    s.name AS ship_name,
    r.name AS room_number,
    r.type,
    r.price
FROM rooms r
JOIN ships s ON r.ship_id = s.id
WHERE r.price BETWEEN 200 AND 500
ORDER BY r.price;

-- =============================================
-- STEP 6: Booking Integration Queries
-- =============================================

-- Get available rooms for a specific ship
-- (Used during booking process)
SELECT 
    r.id,
    r.name AS room_number,
    r.type,
    r.price,
    r.is_available
FROM rooms r
WHERE 
    r.ship_id = 'your-ship-uuid-here'
    AND r.is_available = true
ORDER BY r.price, r.name;

-- Check if specific rooms are available
SELECT 
    r.id,
    r.name,
    r.is_available,
    CASE 
        WHEN r.is_available THEN 'Available'
        ELSE 'Booked'
    END as status
FROM rooms r
WHERE r.id IN ('room-uuid-1', 'room-uuid-2', 'room-uuid-3');

-- Get rooms already booked for a tour instance
SELECT 
    r.id,
    r.name AS room_number,
    r.type,
    COUNT(b.id) AS booking_count
FROM rooms r
LEFT JOIN bookings b ON r.id = ANY(b.selected_rooms::uuid[])
WHERE b.tour_instance_id = 'your-tour-instance-uuid'
GROUP BY r.id, r.name, r.type
ORDER BY booking_count DESC;

-- =============================================
-- STEP 7: Data Integrity Checks
-- =============================================

-- Check for rooms without ships (orphaned records)
SELECT r.* 
FROM rooms r
LEFT JOIN ships s ON r.ship_id = s.id
WHERE s.id IS NULL;

-- Check for duplicate room numbers per ship
SELECT 
    ship_id,
    name,
    COUNT(*) as duplicate_count
FROM rooms
GROUP BY ship_id, name
HAVING COUNT(*) > 1;

-- Check ships exceeding capacity
SELECT 
    s.id,
    s.name AS ship_name,
    s.capacity,
    COUNT(r.id) AS room_count,
    COUNT(r.id) - s.capacity AS over_capacity
FROM ships s
LEFT JOIN rooms r ON s.id = r.ship_id
GROUP BY s.id, s.name, s.capacity
HAVING COUNT(r.id) > s.capacity;

-- Validate room types against room_types table
SELECT DISTINCT r.type
FROM rooms r
WHERE NOT EXISTS (
    SELECT 1 FROM room_types rt 
    WHERE rt.name = r.type
);

-- =============================================
-- STEP 8: Performance & Statistics
-- =============================================

-- Total revenue potential (if all rooms booked)
SELECT 
    s.name AS ship_name,
    COUNT(r.id) AS total_rooms,
    SUM(r.price) AS total_revenue_potential,
    AVG(r.price) AS avg_room_price
FROM ships s
LEFT JOIN rooms r ON s.id = r.ship_id
GROUP BY s.id, s.name
ORDER BY total_revenue_potential DESC;

-- Rooms by availability status
SELECT 
    is_available,
    CASE 
        WHEN is_available THEN 'Available'
        ELSE 'Unavailable'
    END as status,
    COUNT(*) AS count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM rooms), 2) AS percentage
FROM rooms
GROUP BY is_available;

-- Price distribution analysis
SELECT 
    CASE 
        WHEN price < 200 THEN 'Budget (< $200)'
        WHEN price BETWEEN 200 AND 400 THEN 'Standard ($200-$400)'
        WHEN price BETWEEN 400 AND 700 THEN 'Premium ($400-$700)'
        ELSE 'Luxury (> $700)'
    END AS price_category,
    COUNT(*) AS room_count,
    AVG(price) AS avg_price
FROM rooms
GROUP BY price_category
ORDER BY avg_price;

-- =============================================
-- STEP 9: Maintenance Queries
-- =============================================

-- Update all room prices by percentage (e.g., 10% increase)
/*
UPDATE rooms 
SET 
    price = price * 1.10,
    updated_at = NOW()
WHERE type = 'Single'
RETURNING name, type, price;
*/

-- Mark all rooms as available (reset)
/*
UPDATE rooms 
SET 
    is_available = true,
    updated_at = NOW()
RETURNING *;
*/

-- Delete all rooms for a specific ship
/*
DELETE FROM rooms 
WHERE ship_id = 'your-ship-uuid-here'
RETURNING *;
*/

-- Backup rooms data (export to JSON)
SELECT json_agg(row_to_json(rooms)) 
FROM rooms;

-- =============================================
-- STEP 10: Cleanup (Use with Caution!)
-- =============================================

-- Delete all sample data
/*
TRUNCATE TABLE rooms CASCADE;
*/

-- Reset auto-increment and delete all
/*
DELETE FROM rooms;
*/

-- Drop table (DANGEROUS - will lose all data!)
/*
DROP TABLE IF EXISTS rooms CASCADE;
*/

-- =============================================
-- End of Sample Queries
-- =============================================
-- Remember to replace placeholder UUIDs with actual values from your database
-- Always test on a development environment first!
