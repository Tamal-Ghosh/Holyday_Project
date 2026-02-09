-- ShipVoyage Database Schema for Supabase PostgreSQL
-- Run this in: Supabase Dashboard > SQL Editor > New Query

-- Users table
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

-- Ships table
CREATE TABLE IF NOT EXISTS ships (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id TEXT PRIMARY KEY,
    ship_id TEXT NOT NULL REFERENCES ships(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    type TEXT,
    price DECIMAL(10, 2),
    capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Tours table
CREATE TABLE IF NOT EXISTS tours (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    "from" TEXT NOT NULL,
    "to" TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Tour Instances table
CREATE TABLE IF NOT EXISTS tour_instances (
    id TEXT PRIMARY KEY,
    tour_id TEXT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    ship_id TEXT NOT NULL REFERENCES ships(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    available_capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Bookings table
CREATE TABLE IF NOT EXISTS bookings (
    id TEXT PRIMARY KEY,
    tour_instance_id TEXT NOT NULL REFERENCES tour_instances(id) ON DELETE CASCADE,
    room_id TEXT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    email TEXT NOT NULL,
    payment_method TEXT,
    total_payment DECIMAL(10, 2) DEFAULT 0,
    paid_amount DECIMAL(10, 2) DEFAULT 0,
    due_amount DECIMAL(10, 2) DEFAULT 0,
    discount DECIMAL(10, 2) DEFAULT 0,
    status TEXT DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Enable Row Level Security (RLS) on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE ships ENABLE ROW LEVEL SECURITY;
ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE tours ENABLE ROW LEVEL SECURITY;
ALTER TABLE tour_instances ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;

-- Create policies to allow anon key access (for development)
-- WARNING: These policies allow all operations. Adjust for production!

-- Users policies
CREATE POLICY "Allow all on users" ON users FOR ALL USING (true) WITH CHECK (true);

-- Ships policies
CREATE POLICY "Allow all on ships" ON ships FOR ALL USING (true) WITH CHECK (true);

-- Rooms policies
CREATE POLICY "Allow all on rooms" ON rooms FOR ALL USING (true) WITH CHECK (true);

-- Tours policies
CREATE POLICY "Allow all on tours" ON tours FOR ALL USING (true) WITH CHECK (true);

-- Tour instances policies
CREATE POLICY "Allow all on tour_instances" ON tour_instances FOR ALL USING (true) WITH CHECK (true);

-- Bookings policies
-- Bookings policies
CREATE POLICY "Allow all on bookings" ON bookings FOR ALL USING (true) WITH CHECK (true);

-- Insert sample admin user (password: admin123), phone, role, name)
VALUES (
    'admin-001',
    'admin',
    'admin@shipvoyage.com',
    'admin123',
    '+1234567890',
    'admin',
    'System Administrator'
) ON CONFLICT (id) DO NOTHING;

-- Insert sample ships
INSERT INTO ships (id, name, description, capacity) VALUES
('ship-001', 'Ocean Explorer', 'Luxury cruise ship with modern amenities', 500),
('ship-002', 'Sea Breeze', 'Mid-size vessel perfect for island tours', 300),
('ship-003', 'Wave Rider', 'Fast and comfortable touring ship', 200)
ON CONFLICT (id) DO NOTHING;

-- Insert sample tours
INSERT INTO tours (id, name, "from", "to", description) VALUES
('tour-001', 'Mediterranean Cruise', 'Barcelona', 'Athens', 'Experience the beauty of Mediterranean islands'),
('tour-002', 'Caribbean Adventure', 'Miami', 'Nassau', 'Tropical islands and pristine beaches'),
('tour-003', 'Alaskan Expedition', 'Seattle', 'Juneau', 'Glaciers and wildlife in Alaska')
ON CONFLICT (id) DO NOTHING;
