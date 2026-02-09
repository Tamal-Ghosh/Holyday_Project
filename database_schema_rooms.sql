-- =============================================
-- ShipVoyage Database Schema: rooms table
-- =============================================
-- This script creates the rooms table in Supabase PostgreSQL
-- Run this in your Supabase SQL Editor
-- =============================================

-- Create rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ship_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,          -- Room number/name
    type VARCHAR(50) NOT NULL,            -- Room type (references room_types.name)
    price DECIMAL(10, 2) NOT NULL,        -- Room price per person/night
    is_available BOOLEAN DEFAULT true,    -- Availability status
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    
    -- Foreign key constraint (if ships table exists)
    CONSTRAINT fk_ship FOREIGN KEY (ship_id) REFERENCES ships(id) ON DELETE CASCADE,
    
    -- Ensure room numbers are unique per ship
    CONSTRAINT unique_room_per_ship UNIQUE (ship_id, name)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_rooms_ship_id ON rooms(ship_id);
CREATE INDEX IF NOT EXISTS idx_rooms_type ON rooms(type);
CREATE INDEX IF NOT EXISTS idx_rooms_is_available ON rooms(is_available);

-- Enable Row Level Security (RLS)
ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;

-- Create policies for authenticated users (adjust based on your auth requirements)
-- Policy: Allow all authenticated users to read rooms
CREATE POLICY "Enable read access for authenticated users" ON rooms
    FOR SELECT
    USING (auth.role() = 'authenticated');

-- Policy: Allow admin users to insert rooms
CREATE POLICY "Enable insert for authenticated users" ON rooms
    FOR INSERT
    WITH CHECK (auth.role() = 'authenticated');

-- Policy: Allow admin users to update rooms
CREATE POLICY "Enable update for authenticated users" ON rooms
    FOR UPDATE
    USING (auth.role() = 'authenticated');

-- Policy: Allow admin users to delete rooms
CREATE POLICY "Enable delete for authenticated users" ON rooms
    FOR DELETE
    USING (auth.role() = 'authenticated');

-- Optional: Create a function to auto-update updated_at timestamp
CREATE OR REPLACE FUNCTION update_rooms_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at
CREATE TRIGGER trigger_update_rooms_updated_at
    BEFORE UPDATE ON rooms
    FOR EACH ROW
    EXECUTE FUNCTION update_rooms_updated_at();

-- =============================================
-- Sample Data (Optional - for testing)
-- =============================================
-- Uncomment to insert sample rooms
-- Note: Replace ship_id values with actual UUIDs from your ships table

/*
INSERT INTO rooms (ship_id, name, type, price, is_available) VALUES
    ('your-ship-uuid-here', 'A101', 'Single', 150.00, true),
    ('your-ship-uuid-here', 'A102', 'Double', 250.00, true),
    ('your-ship-uuid-here', 'A103', 'Suite', 450.00, true),
    ('your-ship-uuid-here', 'B201', 'Single', 150.00, true),
    ('your-ship-uuid-here', 'B202', 'Double', 250.00, true);
*/

-- =============================================
-- Queries to verify table creation
-- =============================================
-- Check table structure
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'rooms'
ORDER BY ordinal_position;

-- Check indexes
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'rooms';

-- Count rooms
SELECT COUNT(*) as total_rooms FROM rooms;

-- View rooms by ship
-- SELECT r.*, s.name as ship_name
-- FROM rooms r
-- JOIN ships s ON r.ship_id = s.id
-- ORDER BY s.name, r.name;
