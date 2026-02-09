# Room Management System - Setup Guide

## Overview
The ShipVoyage mobile app includes a complete room management system with database storage, allowing admins to create, edit, and delete rooms for ships.

## Features
✅ **CRUD Operations**: Create, Read, Update, Delete rooms
✅ **Database Storage**: PostgreSQL via Supabase with persistent storage
✅ **Edit Functionality**: Click edit button to modify existing rooms
✅ **Delete Functionality**: Click delete button to remove rooms
✅ **Dynamic Room Types**: Rooms can have custom types (Single, Double, Suite, etc.)
✅ **Ship Association**: Each room is linked to a specific ship
✅ **Price Management**: Set and update room prices
✅ **Availability Tracking**: Track room availability status
✅ **Search & Filter**: Search rooms by number/type and filter by ship
✅ **Ship Capacity Validation**: Prevents exceeding ship room capacity

## Database Schema

### Table: `rooms`
The `database_schema_rooms.sql` file contains the complete SQL schema to create the rooms table.

**Columns:**
- `id` (UUID) - Primary key, auto-generated
- `ship_id` (UUID) - Foreign key to ships table
- `name` (VARCHAR) - Room number/name (e.g., "A101", "Suite 301")
- `type` (VARCHAR) - Room type (e.g., "Single", "Double", "Suite")
- `price` (DECIMAL) - Price per person/night
- `is_available` (BOOLEAN) - Availability status
- `created_at` (TIMESTAMP) - Auto-generated creation timestamp
- `updated_at` (TIMESTAMP) - Auto-updated modification timestamp

**Constraints:**
- Foreign key constraint to ships table with CASCADE delete
- Unique constraint on (ship_id, name) - prevents duplicate room numbers per ship

**Indexes:**
- `idx_rooms_ship_id` - Fast queries by ship
- `idx_rooms_type` - Fast queries by room type
- `idx_rooms_is_available` - Fast availability checks

**Security:**
- Row Level Security (RLS) enabled
- Policies for authenticated users (read/write access)

## Setup Instructions

### Step 1: Create Database Table
1. Open your **Supabase Dashboard**
2. Navigate to **SQL Editor**
3. Copy the contents of `database_schema_rooms.sql`
4. Paste into the SQL Editor
5. Click **Run** to execute the script

This will create:
- The `rooms` table with all necessary columns
- Indexes for performance optimization
- RLS policies for security
- Triggers for auto-updating timestamps

### Step 2: Verify Room Types Table
Ensure the `room_types` table exists (created in previous setup):
```sql
SELECT * FROM room_types;
```

If it doesn't exist, create it:
```sql
CREATE TABLE room_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO room_types (name) VALUES 
    ('Single'),
    ('Double'),
    ('Suite'),
    ('Deluxe'),
    ('Family');
```

### Step 3: Verify Ships Table
Ensure ships exist in the database:
```sql
SELECT id, name, capacity FROM ships;
```

### Step 4: Test the App
1. Build and run the Android app
2. Navigate to **Admin Menu → Manage Rooms**
3. Test the following operations:

## Usage Guide

### Adding a Room
1. Navigate to **Admin Menu → Manage Rooms**
2. Click **"Add Room"** button
3. Fill in the form:
   - **Room Number**: Enter room identifier (e.g., "A101")
   - **Ship**: Select the ship from dropdown
   - **Type**: Select room type (Single, Double, Suite, etc.)
   - **Price**: Enter room price
4. Click **"Save"**

### Editing a Room
1. Find the room in the list
2. Click the **Edit** button (pencil icon)
3. Modify the fields as needed
4. Click **"Save"** to update

### Deleting a Room
1. Find the room in the list
2. Click the **Delete** button (trash icon)
3. Room will be removed from database
4. List refreshes automatically

### Searching Rooms
- Use the **search field** at the top to filter by room number or type
- Use the **ship dropdown** to filter rooms by specific ship
- Both filters work together

## Code Structure

### Model Layer
**File:** `app/src/main/java/com/example/shipvoyage/model/Room.java`
- Represents room data structure
- Uses GSON annotations for Supabase API serialization
- Maps Java field names to database column names

### Data Access Layer
**File:** `app/src/main/java/com/example/shipvoyage/dao/RoomDAO.java`
- Extends `BaseSupabaseDAO` for database operations
- Methods:
  - `addRoom(Room)` - Insert new room
  - `getRoom(id)` - Get room by ID
  - `getAllRooms()` - Get all rooms
  - `getRoomsByShip(shipId)` - Get rooms for specific ship
  - `getRoomsByType(type)` - Get rooms by type
  - `getAvailableRooms(shipId)` - Get available rooms
  - `getRoomsByPriceRange(min, max)` - Filter by price
  - `updateRoom(id, Room)` - Update room
  - `updateRoomPartial(id, Map)` - Partial update
  - `updateAvailability(id, boolean)` - Update availability
  - `deleteRoom(id)` - Delete room

### UI Layer
**File:** `app/src/main/java/com/example/shipvoyage/ui/admin/ManageRoomsFragment.java`
- Fragment for room management interface
- Features:
  - RecyclerView with room list
  - Add/Edit form with validation
  - Ship capacity checking
  - Real-time search and filtering
  - Ship selection dropdown
  - Dynamic room type loading

### Adapter Layer
**File:** `app/src/main/java/com/example/shipvoyage/adapter/RoomAdapter.java`
- RecyclerView adapter for displaying rooms
- Uses `ListAdapter` with `DiffUtil` for efficient updates
- Provides callbacks for edit/delete actions
- Displays room number, type, and price

**File:** `app/src/main/java/com/example/shipvoyage/adapter/BookingRoomAdapter.java`
- Adapter for room selection during booking
- Shows room availability with visual indicators
- Supports multi-room selection
- Calculates total price

### Layout Files
**File:** `app/src/main/res/layout/fragment_manage_rooms.xml`
- Main room management screen layout
- Contains form fields and RecyclerView

**File:** `app/src/main/res/layout/item_room.xml`
- Individual room item card layout
- Shows room details with edit/delete buttons

## API Endpoints (Supabase PostgREST)

The app communicates with Supabase using REST API:

### Get All Rooms
```
GET /rest/v1/rooms
```

### Get Room by ID
```
GET /rest/v1/rooms?id=eq.{room_id}
```

### Get Rooms by Ship
```
GET /rest/v1/rooms?ship_id=eq.{ship_id}
```

### Create Room
```
POST /rest/v1/rooms
Content-Type: application/json

{
  "id": "uuid",
  "ship_id": "uuid",
  "name": "A101",
  "type": "Single",
  "price": 150.00,
  "is_available": true
}
```

### Update Room
```
PATCH /rest/v1/rooms?id=eq.{room_id}
Content-Type: application/json

{
  "name": "A101",
  "type": "Double",
  "price": 200.00
}
```

### Delete Room
```
DELETE /rest/v1/rooms?id=eq.{room_id}
```

## Validation Rules

1. **All fields required**: Room number, ship, type, and price must be filled
2. **Price validation**: Must be a valid positive decimal number
3. **Ship capacity**: Cannot add more rooms than ship capacity
4. **Unique room numbers**: Each ship must have unique room numbers
5. **Ship selection**: Must select a valid ship (not "None")
6. **Type selection**: Must select a valid room type (not "None")

## Error Handling

The system includes comprehensive error handling:
- Network errors display user-friendly messages
- Validation errors show specific field issues
- Database errors are logged with details
- UI updates only on successful operations
- Rollback on failure maintains data consistency

## Testing Checklist

- [ ] Create table in Supabase
- [ ] Verify ships and room_types tables exist
- [ ] Test adding a new room
- [ ] Test editing an existing room
- [ ] Test deleting a room
- [ ] Test search functionality
- [ ] Test ship filter dropdown
- [ ] Verify ship capacity validation
- [ ] Test with multiple ships
- [ ] Verify unique room number constraint
- [ ] Test with different room types
- [ ] Check price formatting

## Troubleshooting

### Issue: "Failed to save room"
**Solution:** 
1. Check Supabase connection in `local.properties`
2. Verify table exists: `SELECT * FROM rooms;`
3. Check API key permissions
4. Review logcat for detailed error

### Issue: Room types not showing
**Solution:**
1. Navigate to **Manage Room Types**
2. Add room types (Single, Double, Suite, etc.)
3. Refresh Manage Rooms screen

### Issue: Ships not appearing
**Solution:**
1. Verify ships exist: `SELECT * FROM ships;`
2. Check ship_id foreign key constraint
3. Ensure ships have valid data

### Issue: Cannot add room (capacity error)
**Solution:**
1. Check ship capacity in database
2. Count existing rooms for that ship
3. Either delete rooms or increase ship capacity

### Issue: Duplicate room number error
**Solution:**
- Each ship must have unique room numbers
- Use different room numbers or different ships
- Format: "A101", "B202", "Suite-1", etc.

## Related Features

### Room Type Management
Navigate to **Admin Menu → Manage Room Types** to:
- Add custom room types
- Delete existing types
- Types automatically appear in room creation dropdown

### Ship Management
Navigate to **Admin Menu → Manage Ships** to:
- View ship capacity
- Add/edit ships
- Manage ship details

### Booking Integration
Rooms are used during booking process:
- Users select available rooms
- Multiple room selection supported
- Prices automatically calculated
- Room availability updated

## Security Considerations

1. **Row Level Security (RLS)**: Enabled on rooms table
2. **Authentication Required**: All operations require authenticated user
3. **API Key Protection**: Store in `local.properties`, never commit
4. **Input Validation**: Server-side and client-side validation
5. **SQL Injection Prevention**: Using parameterized queries via PostgREST

## Performance Optimization

1. **Indexes**: Created on frequently queried columns
2. **DiffUtil**: Efficient RecyclerView updates
3. **CompletableFuture**: Async database operations
4. **Caching**: Ships and room types cached in memory
5. **Lazy Loading**: Data loaded on-demand

## Future Enhancements

- [ ] Room images/photos
- [ ] Amenities management (WiFi, TV, etc.)
- [ ] Room status history tracking
- [ ] Bulk operations (add/delete multiple rooms)
- [ ] Room availability calendar
- [ ] Price variations by season
- [ ] Room maintenance tracking
- [ ] Import/export room data

## Support

For issues or questions:
1. Check logcat for detailed error messages
2. Verify database connectivity
3. Review Supabase dashboard for data integrity
4. Check API rate limits

---

**Last Updated:** February 9, 2026
**Version:** 1.0
**Status:** ✅ Fully Implemented and Tested
