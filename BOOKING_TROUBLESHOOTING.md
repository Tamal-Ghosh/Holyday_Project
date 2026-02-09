# Booking Failure Troubleshooting Guide

## Issue: "why booking is failed"

The message you're seeing (`onUnbind: Intent { ... com.google.android.gms ...}`) is **NOT AN ERROR**. This is a normal Google Play Services background message.

However, if bookings are actually failing to save, follow this checklist:

---

## CRITICAL FIX - Run This SQL First

**1. Open Supabase SQL Editor**
**2. Run the file: `database_fix_bookings.sql`**

This adds the missing `payment_details` column that the app is trying to save.

---

## Common Failure Reasons & Solutions

### 1. **Database Schema Mismatch** (Most Likely)
**Problem:** Bookings table missing `payment_details`, `adult_count`, or `child_count` columns

**Solution:**
```sql
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS payment_details TEXT;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS adult_count INTEGER DEFAULT 1;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS child_count INTEGER DEFAULT 0;
```

**How to Verify:**
- Open Supabase Dashboard → SQL Editor
- Run: `SELECT * FROM information_schema.columns WHERE table_name = 'bookings';`
- Check that all columns exist

---

### 2. **Foreign Key Constraint Violations**
**Problem:** Invalid `tour_instance_id` or `room_id` that doesn't exist in database

**How to Check:**
- When adding booking, make sure:
  - Tour instance is selected from the spinner
  - Room is selected from the room selection dialog
  
**Debug in Logcat:**
```
Look for: "Booking save returned false - possible database constraint violation"
```

---

### 3. **NULL Constraint Violations**
**Problem:** Required fields (name, phone) are empty

**Already Fixed:** App validates name and phone before saving

---

### 4. **Data Type Mismatches**
**Problem:** Database expects TEXT for IDs but app sends UUID format

**Check Your Database:**
```sql
SELECT 
    column_name, 
    data_type 
FROM information_schema.columns 
WHERE table_name = 'bookings' 
AND column_name IN ('id', 'tour_instance_id', 'room_id');
```

**Should be:** All should be `TEXT` or `character varying`

---

## How to Debug the Actual Error

### Step 1: Enable Logcat
1. Connect your Android device or start emulator
2. In Android Studio, open **Logcat** (View → Tool Windows → Logcat)
3. Filter by: `ManageBookings`

### Step 2: Try to Create a Booking
1. Open the app
2. Go to Manage Bookings
3. Select a tour instance
4. Click "Add Booking"
5. Fill in the form
6. Click Save

### Step 3: Read the Logs
Look for these messages in Logcat:

```
✅ SUCCESS:
D/ManageBookings: Saving booking: ...
   (booking details will be shown)
Toast: "Booking saved successfully"

❌ FAILURE - Database Error:
E/BaseSupabaseDAO: Insert failed - Code: 400, Message: ..., Body: ...
Toast: "Failed to save booking - Check Logcat for details"

❌ FAILURE - Network/Exception:
E/ManageBookings: Booking save exception: ...
Toast: "Error: [error message]"
```

---

## Common Error Codes & Meanings

| HTTP Code | Meaning | Solution |
|-----------|---------|----------|
| **400** | Bad Request - Schema mismatch or invalid data | Run `database_fix_bookings.sql` |
| **401** | Unauthorized - Supabase API key invalid | Check `local.properties` |
| **409** | Conflict - Duplicate ID or constraint violation | Use unique booking IDs |
| **500** | Server Error - Database constraint or trigger failed | Check foreign keys exist |

---

## Quick Test Steps

### 1. Check Database Schema
```sql
-- Run in Supabase SQL Editor
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'bookings'
ORDER BY ordinal_position;
```

**Expected Output Must Include:**
- `payment_details` (text, nullable)
- `adult_count` (integer, nullable or DEFAULT 1)
- `child_count` (integer, nullable or DEFAULT 0)

### 2. Check Foreign Keys Exist
```sql
-- Verify the tour instance exists
SELECT id, tour_id FROM tour_instances LIMIT 5;

-- Verify rooms exist
SELECT id, ship_id, name FROM rooms LIMIT 5;
```

### 3. Test Manual Insert
```sql
-- Try inserting a test booking manually
INSERT INTO bookings (
    id, tour_instance_id, room_id, name, phone, email,
    payment_method, payment_details, total_payment, paid_amount, due_amount,
    adult_count, child_count, status
) VALUES (
    'test-123', 
    '<valid-tour-instance-id>',  -- Replace with actual ID
    '<valid-room-id>',            -- Replace with actual ID
    'Test Customer',
    '1234567890',
    'test@example.com',
    'Cash',
    'Test payment details',
    1000.00,
    500.00,
    500.00,
    2,
    1,
    'PENDING'
);
```

If this fails, you'll see the exact error message.

---

## Still Not Working?

### Get the Exact Error:
1. Build and run the app: `.\gradlew assembleDebug`
2. Install on device/emulator
3. Try adding a booking
4. Copy the **full Logcat output** and share it

### Check These Files:
- `app/src/main/java/com/example/shipvoyage/util/SupabaseClient.java` - API key correct?
- `app/google-services.json` - Google services configured?
- `local.properties` - Supabase URL and key set?

---

## Important Notes

1. **The `onUnbind` message is NORMAL** - It's just Google Play Services cleaning up. Not an error.

2. **Database changes require SQL** - You must run the SQL updates in Supabase, the app cannot alter database schema.

3. **Test on real data** - Make sure:
   - At least one ship exists
   - At least one tour exists  
   - At least one tour instance exists
   - At least one room exists for selected ship

4. **Check Supabase Dashboard**:
   - Go to Table Editor → bookings
   - See if any bookings appear after saving
   - If they appear, the app works! If not, check Authentication/RLS policies

---

## Next Steps

1. ✅ Run `database_fix_bookings.sql` in Supabase
2. ✅ Rebuild app: `.\gradlew clean assembleDebug`
3. ✅ Test booking again
4. ✅ Check Logcat for detailed error (filter: "ManageBookings")
5. ✅ Share the Logcat output if still failing
