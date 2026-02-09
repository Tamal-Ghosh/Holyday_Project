<<<<<<< HEAD
# ShipVoyage (Android) - Admin Panel Only

ShipVoyage is an admin-only cruise management Android app built with Java, Supabase PostgreSQL, and Material Design 3. It provides real-time admin management of tours, ships, rooms, schedules, bookings, and customers.

## Features
- **Admin Dashboard**: Complete cruise management system
- **Tour Management**: Create, edit, delete tours with details
- **Ship Management**: Manage ships and their specifications
- **Room Management**: Configure rooms with pricing and capacity
- **Tour Instances**: Schedule and manage tour departures
- **Booking Management**: View all bookings with details
- **Customer Management**: Track all customer accounts
- **Featured Photos**: Manage promotional images
- **Real-time Sync**: Supabase PostgreSQL with REST API
- **Authentication**: Email/password login for admin accounts

## Architecture
- **Single Admin Activity**: `AdminMainActivity` with Navigation Component for fragment flows
- **DAO Layer**: Supabase REST API CRUD operations
  - `BaseSupabaseDAO`: Async operations using `CompletableFuture`
  - Type-safe queries with Gson JSON serialization
  - OkHttp3 for HTTP requests with automatic header injection
- **Threading**: Background async operations with ExecutorService
- **UI Framework**: RecyclerView + DiffUtil for efficient list updates
- **Design System**: Material Design 3 with unified card styling

## Tech Stack
- **Language**: Java 11
- **Android**: minSdk 24, targetSdk 36
- **Backend**: Supabase (PostgreSQL) with REST API
- **Database**: PostgreSQL via Supabase PostgREST
- **HTTP Client**: OkHttp3 v4.11.0
- **JSON**: Gson v2.10.1
- **UI**: AndroidX, Material Design 3, ConstraintLayout, Navigation Component, RecyclerView
- **Async**: Kotlin Coroutines, CompletableFuture

## Project Structure (high level)
```
app/src/main/
	# ShipVoyage (Android)

	ShipVoyage is a dual-role cruise booking app (Admin & Passenger) built with Java, Firebase, and Material Design 3. It covers tour discovery, color-coded room selection, secure payments (Visa/bKash), and real-time admin control over tours, ships, rooms, schedules, bookings, customers, and featured photos.

	**Highlights**
	- Dual portals: Admin dashboard + Passenger booking experience
	- Realtime: Firebase Auth + Realtime Database for live data and sessions
	- Booking integrity: PENDING vs CONFIRMED; rooms lock only after payment
	- Room UX: Color legend (Single/Double/Selected/Booked) with capacity checks
	- Payments: Visa and bKash forms with validation before confirmation
## Project Structure

```
app/src/main/
	java/com/example/shipvoyage/
		MainActivity.java              # Entry point (role selection)
		ui/auth/                       # Login, Signup
			LoginActivity.java
			SignupActivity.java
			UserTypeActivity.java
		ui/admin/                      # Admin dashboard & management
			AdminMainActivity.java
			AdminDashboardActivity.java
			ManageShipsActivity.java
			ManageRoomsActivity.java
			ManageToursActivity.java
			ManageTourInstancesActivity.java
			ManageFeaturedPhotosActivity.java
			ViewBookingsActivity.java
			CustomerListActivity.java
			AdminProfileActivity.java
		adapter/                       # RecyclerView adapters (DiffUtil)
		dao/                           # Supabase data access objects
			BaseSupabaseDAO.java       # Async REST API base class
			UserDAO.java
			TourDAO.java
			ShipDAO.java
			RoomDAO.java
			TourInstanceDAO.java
			BookingDAO.java
			FeaturedPhotoDAO.java
		model/                         # Data models
			User.java
			Tour.java
			Ship.java
			Room.java
			TourInstance.java
			Booking.java
			FeaturedPhoto.java
		util/                          # Utilities
			SupabaseClient.java        # Supabase configuration
			ThreadPool.java
	res/
		layout/                        # Activities and fragments
		navigation/                    # Admin nav graph only
		values/                        # Colors, strings, styles, dimens
```

## Prerequisites

- Android Studio Flamingo/Koala or newer
- JDK 11
- Android SDK 36 installed
- Supabase project with PostgreSQL database
- Supabase anon key for API access

## Setup & Configuration

### 1. Get Supabase Credentials
- Create project at https://app.supabase.com
- Project Settings → API Keys → Copy "anon" key
- API URL: `https://dgyonbbyifaqsffbzdpk.supabase.co` (already configured)

### 2. Update SupabaseClient
Replace in `SupabaseClient.java`:
```java
private static final String SUPABASE_ANON_KEY = "your-actual-anon-key";
```

### 3. Create Database Tables
Run the SQL schema in Supabase SQL Editor (see MIGRATION_GUIDE.md for full schema)

### 4. Build & Run
```bash
# Sync Gradle
./gradlew sync

# Build debug APK
./gradlew assembleDebug

# Install and run
./gradlew installDebug
```

## Running the App

1. **Launch App**: Starts at `MainActivity` (admin role selection)
2. **Login**: Enter admin credentials
3. **Admin Dashboard**: Full management of:
   - Tours
   - Ships
   - Rooms
   - Tour Instances
   - Bookings
   - Customers
   - Featured Photos
4. **Logout**: Return to login screen

## Build Output

```
app/build/outputs/apk/debug/app-debug.apk
```

## API Integration

All database operations use Supabase REST API (PostgREST) via:
- **Base URL**: `https://dgyonbbyifaqsffbzdpk.supabase.co/rest/v1`
- **HTTP Client**: OkHttp3 with automatic header injection
- **Auth Headers**: apikey and Authorization Bearer token
- **Response Format**: JSON (Gson serialization)

Example query:
```
GET /rest/v1/users?email=eq.admin@example.com
Authorization: Bearer <ANON_KEY>
apikey: <ANON_KEY>
```

## Changes from Original

**Removed:**
- Firebase Realtime Database dependency
- Firebase Authentication
- Passenger portal (entire ui/passenger folder)
- Payment system (Visa/bKash)
- Booking flow and customer UI
- google-services.json
- google-services Gradle plugin

**Added:**
- Supabase PostgreSQL backend with REST API
- BaseSupabaseDAO for async REST operations
- CompletableFuture-based async patterns
- SupabaseClient singleton configuration
- Admin-only authentication flow
- OkHttp3 for HTTP requests
- Gson for JSON serialization

**Updated:**
- All 7 DAOs to use Supabase instead of Firebase
- LoginActivity and SignupActivity for email-based auth
- MainActivity to only show admin option
- AndroidManifest.xml (removed passenger activities, updated launcher)
- All layout files (removed passenger sections)

## Admin Workflow

1. **Login** → Email/password authentication via Supabase
2. **Dashboard** → View all statistics and quick actions
3. **Manage Tours** → Create, edit, delete cruise tours
4. **Manage Ships** → Configure ships and specifications
5. **Manage Rooms** → Set up cabins with pricing and capacity
6. **Tour Instances** → Schedule departures and availability
7. **View Bookings** → Track all customer reservations
8. **Customers** → Manage customer accounts and details
9. **Featured Photos** → Upload and manage promotional images
10. **Profile** → Admin account settings

## Implementation Notes

- **Async Operations**: All database calls are non-blocking using `CompletableFuture`
- **JSON Serialization**: Automatic Gson conversion for API requests/responses
- **Error Handling**: Try-catch blocks with logging; user feedback via Toast
- **Threading**: Background operations on ExecutorService; UI updates on main thread
- **Security**: Supabase row-level security (RLS) policies recommended in production
- **API Rate Limiting**: Supabase default limits apply; adjust as needed

## Troubleshooting

### "Failed to fetch data" errors
- Verify Supabase anon key is correct in SupabaseClient.java
- Check database tables exist with correct column names (snake_case)
- Verify RLS policies allow public access (if needed)

### Network timeouts
- Check network connectivity
- Increase timeout values in SupabaseClient (currently 30s)
- Verify Supabase project is active

### Login failures
- Ensure user exists in Supabase users table
- Verify password is correct (currently plaintext; add hashing in production)
- Check user role is "admin"

## License

MIT (see `LICENSE`)

## Migration from Firebase

See `MIGRATION_GUIDE.md` for detailed migration information from Firebase to Supabase.
````
=======
# Holyday_Project
>>>>>>> 0ad1e1681dc8d94530bfaba55fe058803001256f
