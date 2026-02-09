-- =============================================
-- QUICK FIX - Run This Now in Supabase!
-- =============================================
-- Error: "Could not find the 'discount' column of 'bookings'"
-- =============================================

-- Add the missing discount column
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS discount NUMERIC(10,2) DEFAULT 0;

-- Add other missing columns
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS payment_details TEXT;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS adult_count INTEGER DEFAULT 1;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS child_count INTEGER DEFAULT 0;

-- Update existing records
UPDATE bookings SET discount = 0 WHERE discount IS NULL;
UPDATE bookings SET adult_count = 1 WHERE adult_count IS NULL;
UPDATE bookings SET child_count = 0 WHERE child_count IS NULL;

-- Verify the fix
SELECT column_name FROM information_schema.columns 
WHERE table_name = 'bookings' 
AND column_name IN ('discount', 'payment_details', 'adult_count', 'child_count');

-- You should see all 4 columns listed above
-- Then try adding a booking again - it will work!
