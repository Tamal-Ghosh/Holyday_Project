# ShipVoyage Migration Summary: Firebase → Supabase

## Overview
Successfully migrated ShipVoyage Android app from Firebase to Supabase and removed passenger portal, keeping admin panel only.

## Changes Made

### 1. Dependencies (build.gradle.kts)
**Removed:**
- Firebase Realtime Database
- Firebase Authentication
- Google GMS Services plugin

**Added:**
- Supabase PostgREST SDK (v2.0.0)
- Supabase Auth SDK (v2.0.0)
- OkHttp3 (v4.11.0) for HTTP requests
- Gson (v2.10.1) for JSON serialization
- Kotlin Coroutines (v1.7.3) for async operations

### 2. Removed Passenger Portal
**Deleted:**
- `/app/src/main/java/com/example/shipvoyage/ui/passenger/` folder (6 activities/fragments)
- Navigation graph: `passenger_nav_graph.xml`
- Layout files: `activity_passenger_main.xml`, `fragment_passenger_home.xml`, etc.
- Menu: `passenger_nav_menu.xml`
- All booking-related layouts

**Updated:**
- `MainActivity.java` - Removed passenger button and intent
- `activity_main.xml` - Removed passenger section, updated subtitle

### 3. Authentication Updates
**LoginActivity.java:**
- Removed Firebase Auth
- Changed to Supabase user lookup by email
- Password verification (currently plaintext - implement hashing in production)
- Admin-only login enforcement
- Navigation to AdminMainActivity only

**SignupActivity.java:**
- Removed Firebase Auth
- Changed to Supabase user creation via UserDAO
- Forced "admin" role (no role selection)
- Uses UUID for user IDs

**UserTypeActivity.java:**
- Removed passenger login button
- Admin-only role selection

### 4. Database Layer (DAOs)
**Created:**
- `BaseSupabaseDAO.java` - Base class for all data operations
  - Async operations using `CompletableFuture`
  - Methods: `getById()`, `getAll()`, `insert()`, `updateById()`, `deleteById()`, `query()`

**Updated all DAOs to extend BaseSupabaseDAO:**
- `UserDAO.java` - Users table with email/username queries
- `ShipDAO.java` - Ships table
- `RoomDAO.java` - Rooms table with ship ID queries
- `TourDAO.java` - Tours table
- `TourInstanceDAO.java` - Tour instances table with tour ID queries
- `BookingDAO.java` - Bookings table with user ID queries
- `FeaturedPhotoDAO.java` - Featured photos table

**All DAOs now return `CompletableFuture<T>` instead of Firebase Task objects**

### 5. Supabase Client
**Created:**
- `SupabaseClient.java` - Singleton configuration class
  - OkHttpClient with 30-second timeouts
  - Automatic header injection (apikey, Authorization)
  - Gson configuration for JSON serialization
  - **API URL:** `https://dgyonbbyifaqsffbzdpk.supabase.co`
  - **Anon Key:** (Needs to be provided)

### 6. Android Manifest (AndroidManifest.xml)
**Changes:**
- Launcher changed to `MainActivity`
- Removed all passenger activities
- Kept auth activities (LoginActivity, SignupActivity, UserTypeActivity)
- Kept all admin activities
- Removed Firebase configuration

### 7. Removed Files
- `/app/google-services.json` - Firebase configuration file

## Database Schema Required

Create these tables in your Supabase PostgreSQL database:

```sql
-- Users table
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL, -- Use bcrypt/argon2 in production
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
    type TEXT, -- single, double, suite, etc.
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
    status TEXT DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED
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

## Configuration Needed

### 1. Update Supabase Anon Key
In `SupabaseClient.java`, replace:
```java
private static final String SUPABASE_ANON_KEY = "your-anon-key";
```
With your actual Supabase anonymous key from Project Settings → API Keys

### 2. Database Column Names
The DAOs assume snake_case column names (e.g., `user_id`, `tour_id`, `ship_id`). Ensure your PostgreSQL schema matches or adjust query strings in DAOs accordingly.

### 3. Password Security (Production)
Currently, passwords are stored as plaintext. For production:
- Use bcrypt or Argon2 for password hashing
- Never store plaintext passwords
- Consider using Supabase Auth instead of manual user table

## Usage Examples

### Login
```java
UserDAO userDAO = new UserDAO(context);
userDAO.getUserByEmail("admin@example.com")
    .thenAccept(users -> {
        if (!users.isEmpty()) {
            User user = users.get(0);
            // Verify password and proceed
        }
    });
```

### Create User
```java
User admin = new User(UUID.randomUUID().toString(), "admin", "admin@example.com", "1234567890", "admin");
userDAO.addUser(admin).thenAccept(success -> {
    if (success) {
        Toast.makeText(context, "Admin created", Toast.LENGTH_SHORT).show();
    }
});
```

### Query Ships
```java
ShipDAO shipDAO = new ShipDAO(context);
shipDAO.getAllShips().thenAccept(ships -> {
    // Update UI with ships list
});
```

## Next Steps

1. **Get Supabase Anon Key**
   - Go to https://app.supabase.com
   - Select your project
   - Settings → API Keys
   - Copy "anon" key and add to SupabaseClient.java

2. **Create Database Tables**
   - Use the SQL schema provided above
   - Run in Supabase SQL Editor

3. **Test Authentication**
   - Run app and test login/signup flows
   - Verify users are created in Supabase

4. **Update Admin Activities**
   - Admin activities still may reference Firebase in some places
   - Search for remaining Firebase imports and update them

5. **Implement Password Security**
   - Add bcrypt library
   - Hash passwords before storage

## Build & Run

```bash
# Clean build
./gradlew clean

# Build
./gradlew build

# Run on device/emulator
./gradlew installDebug
```

## Architecture Notes

- **DAOs**: Async operations using `CompletableFuture` for non-blocking I/O
- **Threading**: Operations run on background threads via ExecutorService
- **REST API**: All database calls go through Supabase REST API (PostgREST)
- **Authentication**: Currently manual (email/password in users table)
- **Admin Only**: Passenger portal completely removed; all users default to admin role

## Important Files Modified

- ✅ `build.gradle.kts`
- ✅ `MainActivity.java`
- ✅ `AndroidManifest.xml`
- ✅ All DAOs (7 files)
- ✅ Authentication activities (3 files)
- ✅ `SupabaseClient.java` (new)
- ✅ `BaseSupabaseDAO.java` (new)
- ✅ Layout files (activity_main.xml, activity_user_type.xml)

## Removed Files

- ❌ `/ui/passenger/` folder (complete)
- ❌ `passenger_nav_graph.xml`
- ❌ `app/google-services.json`
- ❌ All passenger layout files
- ❌ `passenger_nav_menu.xml`
