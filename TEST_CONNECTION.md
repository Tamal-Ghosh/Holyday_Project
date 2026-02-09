# Testing Supabase Connection

## Current Issue
Your API key `sb_publishable_dclDH30ayLJAkhNh0TwpIg_fN1Pzx6G` is INVALID.

A real Supabase anon key is a JWT token (~200+ characters) that looks like:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRneW9uYmJ5aWZhcXNmZmJ6ZHBrIiwicm9sZSI6ImFub24iLCJpYXQiOjE2OTk5OTk5OTksImV4cCI6MTk5OTk5OTk5OX0.abc123xyz...
```

## Steps to Fix

### 1. Get Your Real Anon Key
1. Visit: https://supabase.com/dashboard/project/dgyonbbyifaqsffbzdpk/settings/api
2. Copy the **anon public** key (NOT the service_role key)
3. The key should be ~200+ characters starting with `eyJ`

### 2. Update SupabaseClient.java
Replace `PASTE_YOUR_REAL_ANON_KEY_HERE` in:
```
app/src/main/java/com/example/shipvoyage/util/SupabaseClient.java
```

### 3. Test Methods

#### A. Test via Android App (Recommended)
1. Rebuild: `.\gradlew.bat assembleDebug`
2. Install on device/emulator
3. Login as admin
4. Navigate to "Manage Ships"
5. Try to add a ship
6. Watch for detailed error messages

#### B. Test via Logcat
```powershell
adb logcat | Select-String "Supabase|BaseSupabaseDAO|HTTP"
```

#### C. Test via PowerShell (After getting real key)
```powershell
# Replace YOUR_REAL_KEY with actual anon key
$key = "YOUR_REAL_KEY_HERE"
$url = "https://dgyonbbyifaqsffbzdpk.supabase.co/rest/v1/ships?select=*&limit=5"

Invoke-WebRequest -Uri $url -Headers @{
    "apikey" = $key
    "Authorization" = "Bearer $key"
} | Select-Object -ExpandProperty Content
```

## Expected Results

### If Connection Works:
- ✅ Ships load in the list
- ✅ You can add new ships
- ✅ Toast message: "Ship saved successfully"
- ✅ Logcat shows: "HTTP 200" or "HTTP 201"

### If Connection Fails:
- ❌ Toast shows detailed error (now with improved error handling)
- ❌ Logcat shows: "401 Unauthorized" or "Invalid API key"
- ❌ Empty ship list or "Failed to load ships"

## Common Errors

### "Invalid API key"
→ The anon key is wrong or expired. Get new one from dashboard.

### "JWT expired"
→ Your project's JWT has expired. Generate new keys in Supabase dashboard.

### "relation 'ships' does not exist"
→ Database table not created. Run the SQL schema from QUICKSTART.md

### Network timeout
→ Check internet connection or firewall settings

## Database Schema Check

Make sure your `ships` table exists in Supabase:

```sql
-- Check if table exists
SELECT * FROM ships LIMIT 1;

-- If not, create it:
CREATE TABLE ships (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);
```

Run this in: Supabase Dashboard > SQL Editor
