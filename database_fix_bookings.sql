-- =============================================
-- CRITICAL: Booking Table Schema Fix
-- =============================================
-- This fixes the bookings table to match the app's Booking model
-- Run this in your Supabase SQL Editor IMMEDIATELY
-- =============================================

-- 1. Add missing discount column (CRITICAL - App is failing because of this)
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS discount NUMERIC(10,2) DEFAULT 0;

-- 2. Add missing payment_details column
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS payment_details TEXT;

-- 3. Ensure adult_count and child_count columns exist
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS adult_count INTEGER DEFAULT 1;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS child_count INTEGER DEFAULT 0;

-- 3. Verify all required columns exist
SELECT 
    column_name, 
    data_type, 
    column_default,
    is_nullable
FROM information_schema.columns
WHERE table_name = 'bookings' 
ORDER BY ordinal_position;

-- 4. Update existing bookings to have default values
UPDATE bookings SET discount = 0 WHERE discount IS NULL;
UPDATE bookings SET adult_count = 1 WHERE adult_count IS NULL;
UPDATE bookings SET child_count = 0 WHERE child_count IS NULL;

-- 5. Check if any bookings exist with NULL in new columns
SELECT 
    COUNT(*) as total_bookings,
-- =============================================
-- Expected Bookings Table Schema:
-- =============================================
-- id                 TEXT PRIMARY KEY
-- tour_instance_id   TEXT NOT NULL
-- room_id            TEXT NOT NULL
-- name               TEXT NOT NULL
-- phone              TEXT NOT NULL
-- email              TEXT (nullable)
-- payment_method     TEXT
-- payment_details    TEXT           <-- MUST EXIST
-- total_payment      NUMERIC(10,2)
-- paid_amount        NUMERIC(10,2)
-- due_amount         NUMERIC(10,2)
-- discount           NUMERIC(10,2)  <-- MUST EXIST (was missing!)
-- adult_count        INTEGER        <-- MUST EXIST
-- child_count        INTEGER        <-- MUST EXIST
-- status             TEXT DEFAULT 'PENDING'
-- created_at         TIMESTAMPTZ DEFAULT NOW()
-- =============================================

-- =============================================
-- ERROR YOU SAW:
-- =============================================
-- Insert failed - Code: 400
-- Message: "Could not find the 'discount' column of 'bookings' in the schema cache"
--
-- CAUSE: Your bookings table was missing the 'discount' column
-- FIX: This script adds it with DEFAULT 0
-- =============================================
-- due_amount         NUMERIC(10,2)
-- adult_count        INTEGER        <-- MUST EXIST
-- child_count        INTEGER        <-- MUST EXIST
-- status             TEXT DEFAULT 'PENDING'
-- created_at         TIMESTAMPTZ DEFAULT NOW()
-- =============================================
