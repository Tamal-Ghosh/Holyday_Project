-- ShipVoyage Complete Database Schema
-- Run this in Supabase SQL Editor: https://supabase.com/dashboard/project/dgyonbbyifaqsffbzdpk/sql/new

-- 1. Create Users table
CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    phone TEXT,
    role TEXT DEFAULT 'passenger',
    name TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    profile_image_path TEXT,
    last_instance TEXT,
    payment_status TEXT
);

-- 2. Create Ships table
CREATE TABLE IF NOT EXISTS ships (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 3. Create Rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id TEXT PRIMARY KEY,
    ship_id TEXT NOT NULL REFERENCES ships(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    type TEXT,
    price DECIMAL(10, 2),
    capacity INT,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 4. Create Tours table
CREATE TABLE IF NOT EXISTS tours (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    duration_days INT,
    base_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 5. Create Tour Instances table
CREATE TABLE IF NOT EXISTS tour_instances (
    id TEXT PRIMARY KEY,
    tour_id TEXT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    ship_id TEXT NOT NULL REFERENCES ships(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    available_capacity INT,
    departure_date DATE,
    status TEXT DEFAULT 'available',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 6. Create Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tour_instance_id TEXT REFERENCES tour_instances(id),
    room_id TEXT REFERENCES rooms(id),
    status TEXT DEFAULT 'PENDING',
    total_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 7. Create Featured Photos table
CREATE TABLE IF NOT EXISTS featured_photos (
    id TEXT PRIMARY KEY,
    title TEXT,
    image_url TEXT,
    tour_id TEXT REFERENCES tours(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Enable Row Level Security (RLS) on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE ships ENABLE ROW LEVEL SECURITY;
ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE tours ENABLE ROW LEVEL SECURITY;
ALTER TABLE tour_instances ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE featured_photos ENABLE ROW LEVEL SECURITY;

-- Create RLS Policies to allow anon key access (for development)
-- WARNING: These policies allow all operations. For production, implement proper authorization.

CREATE POLICY "Allow all on users" ON users FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on ships" ON ships FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on rooms" ON rooms FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on tours" ON tours FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on tour_instances" ON tour_instances FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on bookings" ON bookings FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on featured_photos" ON featured_photos FOR ALL USING (true) WITH CHECK (true);

-- Insert sample admin user
INSERT INTO users (id, username, email, password, phone, role, name) VALUES 
('admin-001', 'admin', 'admin@shipvoyage.com', 'admin123', '+1234567890', 'admin', 'System Administrator')
ON CONFLICT (id) DO NOTHING;

-- Insert sample ships
INSERT INTO ships (id, name, description, capacity) VALUES
('ship-001', 'Ocean Explorer', 'Luxury cruise ship with modern amenities', 500),
('ship-002', 'Sea Breeze', 'Mid-size vessel perfect for island tours', 300),
('ship-003', 'Wave Rider', 'Fast and comfortable touring ship', 200)
ON CONFLICT (id) DO NOTHING;

-- Insert sample rooms for each ship
-- Rooms for Ocean Explorer (500 capacity)
INSERT INTO rooms (id, ship_id, name, type, price, capacity, is_available) VALUES
('room-001', 'ship-001', 'Suite 101', 'Double', 150.00, 2, true),
('room-002', 'ship-001', 'Suite 102', 'Double', 150.00, 2, true),
('room-003', 'ship-001', 'Suite 103', 'Single', 100.00, 1, true),
('room-004', 'ship-001', 'Suite 104', 'Double', 150.00, 2, true),
('room-005', 'ship-001', 'Suite 105', 'Single', 100.00, 1, true)
ON CONFLICT (id) DO NOTHING;

-- Rooms for Sea Breeze (300 capacity)
INSERT INTO rooms (id, ship_id, name, type, price, capacity, is_available) VALUES
('room-101', 'ship-002', 'Room 201', 'Double', 120.00, 2, true),
('room-102', 'ship-002', 'Room 202', 'Single', 80.00, 1, true),
('room-103', 'ship-002', 'Room 203', 'Double', 120.00, 2, true)
ON CONFLICT (id) DO NOTHING;

-- Rooms for Wave Rider (200 capacity)
INSERT INTO rooms (id, ship_id, name, type, price, capacity, is_available) VALUES
('room-201', 'ship-003', 'Cabin A1', 'Single', 70.00, 1, true),
('room-202', 'ship-003', 'Cabin A2', 'Double', 100.00, 2, true)
ON CONFLICT (id) DO NOTHING;

-- Insert sample tours
INSERT INTO tours (id, name, description, duration_days, base_price) VALUES
('tour-001', 'Mediterranean Cruise', 'Experience the beauty of Mediterranean islands', 7, 1200.00),
('tour-002', 'Caribbean Adventure', 'Tropical islands and pristine beaches', 5, 900.00),
('tour-003', 'Alaskan Expedition', 'Glaciers and wildlife in Alaska', 10, 2000.00)
ON CONFLICT (id) DO NOTHING;

-- Insert sample tour instances
INSERT INTO tour_instances (id, tour_id, ship_id, start_date, end_date, available_capacity, departure_date, status) VALUES
('instance-001', 'tour-001', 'ship-001', '2026-03-01', '2026-03-08', 480, '2026-03-01', 'available'),
('instance-002', 'tour-002', 'ship-002', '2026-04-15', '2026-04-20', 280, '2026-04-15', 'available'),
('instance-003', 'tour-003', 'ship-003', '2026-05-10', '2026-05-20', 190, '2026-05-10', 'available')
ON CONFLICT (id) DO NOTHING;

-- Verify all tables created
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' ORDER BY table_name;
