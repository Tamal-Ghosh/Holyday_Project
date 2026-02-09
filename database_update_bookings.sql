-- =============================================
-- Booking Table Updates - Add Adult/Child Counts
-- =============================================

-- Add adult_count and child_count columns to bookings table
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS adult_count INTEGER DEFAULT 1;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS child_count INTEGER DEFAULT 0;

-- Update existing records to have default values
UPDATE bookings SET adult_count = 1 WHERE adult_count IS NULL;
UPDATE bookings SET child_count = 0 WHERE child_count IS NULL;

-- Verify columns were added
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'bookings' 
AND column_name IN ('adult_count', 'child_count');
