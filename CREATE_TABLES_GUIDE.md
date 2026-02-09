# Create Database Tables in Supabase

## Quick Steps:

### 1. Go to Supabase SQL Editor
Visit: https://supabase.com/dashboard/project/dgyonbbyifaqsffbzdpk/sql/new

### 2. Copy ALL the SQL code below

### 3. Paste into the SQL Editor

### 4. Click "Run" button

---

## SQL Code to Run:

```sql
-- Create Users table
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

-- Create Ships table
CREATE TABLE IF NOT EXISTS ships (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create Rooms table
CREATE TABLE IF NOT EXISTS rooms (
    id TEXT PRIMARY KEY,
    ship_id TEXT NOT NULL REFERENCES ships(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    type TEXT,
    price DECIMAL(10, 2),
    capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create Tours table
CREATE TABLE IF NOT EXISTS tours (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    duration_days INT,
    base_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create Tour Instances table
CREATE TABLE IF NOT EXISTS tour_instances (
    id TEXT PRIMARY KEY,
    tour_id TEXT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    ship_id TEXT NOT NULL REFERENCES ships(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    available_capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Create Bookings table
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

-- Create Featured Photos table
CREATE TABLE IF NOT EXISTS featured_photos (
    id TEXT PRIMARY KEY,
    title TEXT,
    image_url TEXT,
    tour_id TEXT REFERENCES tours(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE ships ENABLE ROW LEVEL SECURITY;
ALTER TABLE rooms ENABLE ROW LEVEL SECURITY;
ALTER TABLE tours ENABLE ROW LEVEL SECURITY;
ALTER TABLE tour_instances ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE featured_photos ENABLE ROW LEVEL SECURITY;

-- Create RLS Policies (allow anon key access)
CREATE POLICY "Allow all on users" ON users FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on ships" ON ships FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on rooms" ON rooms FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on tours" ON tours FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on tour_instances" ON tour_instances FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on bookings" ON bookings FOR ALL USING (true) WITH CHECK (true);
CREATE POLICY "Allow all on featured_photos" ON featured_photos FOR ALL USING (true) WITH CHECK (true);

-- Insert sample admin user
INSERT INTO users (id, username, email, password, phone, role, name)
VALUES ('admin-001', 'admin', 'admin@shipvoyage.com', 'admin123', '+1234567890', 'admin', 'System Administrator')
ON CONFLICT (id) DO NOTHING;

-- Insert sample ships
INSERT INTO ships (id, name, description, capacity) VALUES
('ship-001', 'Ocean Explorer', 'Luxury cruise ship with modern amenities', 500),
('ship-002', 'Sea Breeze', 'Mid-size vessel perfect for island tours', 300),
('ship-003', 'Wave Rider', 'Fast and comfortable touring ship', 200)
ON CONFLICT (id) DO NOTHING;
```

---

## Expected Result After Running:

✅ You should see a success message: "Successfully executed SQL"

✅ Tables created:
- users
- ships
- rooms
- tours
- tour_instances
- bookings
- featured_photos

✅ Sample data inserted:
- 1 admin user (username: admin, password: admin123)
- 3 sample ships

---

## After Creating Tables:

1. **Rebuild your app**: `.\gradlew.bat assembleDebug`
2. **Reinstall**: `adb install app\build\outputs\apk\debug\app-debug.apk`
3. **Test adding a ship** - it should now work! ✓

---

## Verify Tables Were Created:

Go to: https://supabase.com/dashboard/project/dgyonbbyifaqsffbzdpk/editor

You should see all 7 tables in the left sidebar under "Tables"
