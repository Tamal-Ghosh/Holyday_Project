# Quick Start Guide - ShipVoyage with Supabase

## ✅ What's Been Done

1. **Removed Firebase completely**
   - Deleted all Firebase dependencies
   - Removed `google-services.json`
   - Removed Firebase imports from auth layer

2. **Removed Passenger Portal**
   - Deleted entire `ui/passenger/` folder
   - Removed passenger navigation graph
   - Removed passenger layout files
   - Removed payment system

3. **Implemented Supabase REST API**
   - `SupabaseClient.java` - Configuration with OkHttp3
   - `BaseSupabaseDAO.java` - Base class for all DAOs
   - Updated all 7 DAOs with async `CompletableFuture` pattern

4. **Admin-Only Authentication**
   - `LoginActivity.java` - Email/password login
   - `SignupActivity.java` - Admin account creation
   - `UserTypeActivity.java` - Role selection (admin only)
   - `MainActivity.java` - Entry point

## 🔧 Configuration Required

### Step 1: Get Your Supabase Anon Key
1. Go to https://app.supabase.com
2. Sign in and select your project
3. Click **Settings** → **API**
4. Copy the **`anon`** key (starts with `eyJ...`)

### Step 2: Update SupabaseClient.java
```java
// File: app/src/main/java/com/example/shipvoyage/util/SupabaseClient.java
// Line ~26

// BEFORE:
private static final String SUPABASE_ANON_KEY = "your-actual-anon-key";

// AFTER:
private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
```

### Step 3: Create Database Tables

Go to Supabase Dashboard → SQL Editor and run:

```sql
-- Users table
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    phone TEXT,
    role TEXT NOT NULL DEFAULT 'admin',
    name TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    profile_image_path TEXT,
    last_instance TEXT,
    payment_status TEXT
);

-- Ships table
CREATE TABLE ships (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Rooms table
CREATE TABLE rooms (
    id TEXT PRIMARY KEY,
    ship_id TEXT NOT NULL REFERENCES ships(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    type TEXT,
    price DECIMAL(10, 2),
    capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Tours table
CREATE TABLE tours (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    duration_days INT,
    base_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Tour Instances table
CREATE TABLE tour_instances (
    id TEXT PRIMARY KEY,
    tour_id TEXT NOT NULL REFERENCES tours(id) ON DELETE CASCADE,
    ship_id TEXT NOT NULL REFERENCES ships(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    available_capacity INT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Bookings table
CREATE TABLE bookings (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tour_instance_id TEXT REFERENCES tour_instances(id),
    room_id TEXT REFERENCES rooms(id),
    status TEXT DEFAULT 'PENDING',
    total_price DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Featured Photos table
CREATE TABLE featured_photos (
    id TEXT PRIMARY KEY,
    title TEXT,
    image_url TEXT,
    tour_id TEXT REFERENCES tours(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW()
);
```

## 🏗️ Build & Run

### Build
```powershell
cd "path\to\ShipVoyage_mobile - Copy (2)"
.\gradlew.bat clean build
```

### Install on Device
```powershell
.\gradlew.bat installDebug
```

### Run from Android Studio
- Open project in Android Studio
- Wait for Gradle sync to complete
- Click **Run** (Shift+F10)

## 📱 Using the App

### 1. Launch
App starts at `MainActivity` → Shows admin option only

### 2. Sign Up (First Time)
- Click **Admin Login**
- Click **Sign up here** link
- Enter:
  - Username
  - Email
  - Phone (optional)
  - Password (6+ chars)
  - Confirm password
- Click **Sign Up** → Creates user in Supabase

### 3. Login
- Back to login page
- Enter email and password
- Click **Login**
- Navigates to **Admin Dashboard**

### 4. Admin Dashboard
Access all management features:
- Tours
- Ships
- Rooms
- Tour Instances
- Bookings
- Customers
- Featured Photos
- Admin Profile

## 🔌 API Integration

### How It Works
1. **All requests** go to: `https://dgyonbbyifaqsffbzdpk.supabase.co/rest/v1/`
2. **Headers automatically added:**
   - `apikey: <your-anon-key>`
   - `Authorization: Bearer <your-anon-key>`
   - `Content-Type: application/json`

3. **Responses** automatically parsed with Gson

### Example: Getting Users
```java
UserDAO userDAO = new UserDAO(context);

// Get all users
userDAO.getAllUsers()
    .thenAccept(users -> {
        // Use users list
        Toast.makeText(context, "Found " + users.size() + " users", Toast.LENGTH_SHORT).show();
    })
    .exceptionally(e -> {
        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        return null;
    });

// Get user by email
userDAO.getUserByEmail("admin@example.com")
    .thenAccept(users -> {
        if (!users.isEmpty()) {
            User user = users.get(0);
            // User found
        }
    });
```

### Example: Creating a Ship
```java
ShipDAO shipDAO = new ShipDAO(context);

Ship ship = new Ship();
ship.setId(UUID.randomUUID().toString());
ship.setName("USS Enterprise");
ship.setDescription("Luxury cruise ship");
ship.setCapacity(2000);

shipDAO.addShip(ship)
    .thenAccept(success -> {
        if (success) {
            Toast.makeText(context, "Ship added!", Toast.LENGTH_SHORT).show();
        }
    });
```

## 🐛 Troubleshooting

### Build Fails: "Could not resolve..."
```bash
# Clean cache and refresh dependencies
.\gradlew.bat clean --refresh-dependencies
.\gradlew.bat build
```

### Login Fails: "User not found"
- Verify user exists in Supabase: Go to Tables → users
- Check email spelling
- Make sure you signed up first

### API Errors: 401 Unauthorized
- Verify anon key in SupabaseClient.java is correct
- Check key isn't expired
- Verify table RLS policies allow public access (default: open)

### API Errors: 400 Bad Request
- Check JSON field names match database column names
- Example: `ship_id` not `shipId`
- Verify data types match (string, int, date, etc.)

### Timeout Errors
- Verify internet connection
- Check Supabase project is running
- Increase timeout in SupabaseClient.java if needed (currently 30s)

## 📋 Project Status

### ✅ Complete
- Firebase removal
- Passenger portal removal
- Supabase integration (DAOs)
- Authentication (login/signup)
- Database schema
- Build fixes

### 🔄 In Development (Admin Activities)
- These still reference Firebase in imports
- Will work with Supabase DAOs once updated
- Async pattern: `.thenAccept()` instead of `.addOnSuccessListener()`

### 📝 Documentation
- `MIGRATION_GUIDE.md` - Full migration details
- `BUILD_FIX_SUMMARY.md` - Build fixes and dependencies
- `README.md` - Project overview

## 💡 Key Differences from Firebase

| Firebase | Supabase |
|----------|----------|
| Real-time listeners | REST API polling |
| Task<Result> | CompletableFuture<T> |
| .addOnSuccessListener() | .thenAccept() |
| push().getKey() | UUID.randomUUID().toString() |
| Realtime Ref.child() | POST to /table |
| Complex queries | PostgREST filters |

## 🚀 Next Steps

1. ✅ Code changes done
2. **TODO:** Update SupabaseClient with anon key
3. **TODO:** Create database tables
4. **TODO:** Test signup/login
5. **TODO:** Update admin activities (Firebase → Supabase DAOs)
6. **TODO:** Test CRUD operations
7. **TODO:** Configure RLS policies (production)
8. **TODO:** Implement password hashing (production)

## 📚 Resources

- **Supabase Docs**: https://supabase.com/docs
- **PostgREST API**: https://postgrest.org/
- **OkHttp3**: https://square.github.io/okhttp/
- **Gson**: https://github.com/google/gson

## Support

For issues:
1. Check `BUILD_FIX_SUMMARY.md` troubleshooting section
2. Verify Supabase anon key is correct
3. Check database tables exist with correct names
4. Verify RLS policies allow operations

Good luck! 🎉
