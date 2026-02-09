# Admin Activities Migration Checklist

## Overview
The auth layer and DAOs are fully migrated to Supabase. Admin activities still contain Firebase imports and need updating to use the new async DAOs.

## Files That Need Updates

### 🔴 Fragments with Firebase Imports (8 files)

- [ ] `AdminDashboardFragment.java` 
  - Imports: FirebaseAuth, DataSnapshot, DatabaseError, ValueEventListener
  - Update: Remove Firebase, use DAOs with CompletableFuture
  - Example change:
    ```java
    // BEFORE:
    database.getReference("tours").addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) { }
    });
    
    // AFTER:
    TourDAO tourDAO = new TourDAO(context);
    tourDAO.getAllTours().thenAccept(tours -> {
        // Update UI with tours
    });
    ```

- [ ] `AdminProfileFragment.java`
  - Imports: FirebaseAuth
  - Update: Use UserDAO for profile management

- [ ] `ManageShipsFragment.java`
  - Imports: DataSnapshot
  - Update: Use ShipDAO with async pattern

- [ ] `ViewBookingsFragment.java`
  - Imports: DataSnapshot
  - Update: Use BookingDAO with async pattern

- [ ] `CustomerListFragment.java`
  - Imports: DataSnapshot
  - Update: Use UserDAO.getAllUsers() with async pattern

- [ ] Other admin fragments (check imports)

### Activities (May have Firebase references)

- [ ] `ManageShipsActivity.java`
- [ ] `ManageRoomsActivity.java`
- [ ] `ManageToursActivity.java`
- [ ] `ManageTourInstancesActivity.java`
- [ ] `ManageFeaturedPhotosActivity.java`
- [ ] `ViewBookingsActivity.java`
- [ ] `CustomerListActivity.java`
- [ ] `AdminProfileActivity.java`

## Migration Pattern

### Step 1: Remove Firebase Imports
```java
// REMOVE these imports:
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.Task;
```

### Step 2: Add DAO Imports
```java
// ADD these imports:
import com.example.shipvoyage.dao.UserDAO;
import com.example.shipvoyage.dao.TourDAO;
import com.example.shipvoyage.dao.ShipDAO;
import com.example.shipvoyage.dao.RoomDAO;
import com.example.shipvoyage.dao.TourInstanceDAO;
import com.example.shipvoyage.dao.BookingDAO;
import com.example.shipvoyage.dao.FeaturedPhotoDAO;
```

### Step 3: Replace Firebase Calls

#### Getting Data
```java
// BEFORE (Firebase):
FirebaseDatabase.getInstance()
    .getReference("tours")
    .addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            List<Tour> tours = new ArrayList<>();
            for (DataSnapshot child : snapshot.getChildren()) {
                Tour tour = child.getValue(Tour.class);
                tours.add(tour);
            }
            updateUI(tours);
        }
        
        @Override
        public void onCancelled(DatabaseError error) { }
    });

// AFTER (Supabase):
TourDAO tourDAO = new TourDAO(this);
tourDAO.getAllTours().thenAccept(tours -> {
    updateUI(tours);
}).exceptionally(e -> {
    Log.e("TAG", "Error loading tours: " + e.getMessage());
    return null;
});
```

#### Creating Data
```java
// BEFORE (Firebase):
FirebaseDatabase.getInstance()
    .getReference("tours")
    .child(tour.getId())
    .setValue(tour)
    .addOnSuccessListener(unused -> {
        Toast.makeText(context, "Tour added", Toast.LENGTH_SHORT).show();
    })
    .addOnFailureListener(e -> {
        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    });

// AFTER (Supabase):
TourDAO tourDAO = new TourDAO(this);
tourDAO.addTour(tour).thenAccept(success -> {
    if (success) {
        Toast.makeText(this, "Tour added", Toast.LENGTH_SHORT).show();
    }
}).exceptionally(e -> {
    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    return null;
});
```

#### Updating Data
```java
// BEFORE (Firebase):
FirebaseDatabase.getInstance()
    .getReference("tours")
    .child(tourId)
    .updateChildren(updates)
    .addOnSuccessListener(unused -> {})
    .addOnFailureListener(e -> {});

// AFTER (Supabase):
TourDAO tourDAO = new TourDAO(this);
tourDAO.updateTour(tourId, updates).thenAccept(success -> {
    // Handle success
});
```

#### Deleting Data
```java
// BEFORE (Firebase):
FirebaseDatabase.getInstance()
    .getReference("tours")
    .child(tourId)
    .removeValue()
    .addOnSuccessListener(unused -> {})
    .addOnFailureListener(e -> {});

// AFTER (Supabase):
TourDAO tourDAO = new TourDAO(this);
tourDAO.deleteTour(tourId).thenAccept(success -> {
    // Handle success
});
```

### Step 4: Update AsyncTask Pattern

If using AsyncTask (avoid in modern Android):
```java
// BEFORE:
new AsyncTask<Void, Void, List<Tour>>() {
    @Override
    protected List<Tour> doInBackground(Void... voids) {
        // Firebase call
        return tours;
    }
    
    @Override
    protected void onPostExecute(List<Tour> tours) {
        updateUI(tours);
    }
}.execute();

// AFTER:
tourDAO.getAllTours()
    .thenAccept(tours -> {
        updateUI(tours);
    })
    .exceptionally(e -> {
        Log.e("TAG", "Error", e);
        return null;
    });
```

## Available DAOs and Methods

### UserDAO
```java
CompletableFuture<Boolean> addUser(User user)
CompletableFuture<User> getUser(String id)
CompletableFuture<List<User>> getAllUsers()
CompletableFuture<List<User>> getUserByEmail(String email)
CompletableFuture<List<User>> getUserByUsername(String username)
CompletableFuture<List<User>> getUsersByRole(String role)
CompletableFuture<Boolean> updateUser(User user)
CompletableFuture<Boolean> updateUser(String id, Map<String, Object> updates)
CompletableFuture<Boolean> deleteUser(String id)
```

### TourDAO
```java
CompletableFuture<Boolean> addTour(Tour tour)
CompletableFuture<Tour> getTour(String tourId)
CompletableFuture<List<Tour>> getAllTours()
CompletableFuture<Boolean> updateTour(String tourId, Map<String, Object> updates)
CompletableFuture<Boolean> deleteTour(String tourId)
```

### ShipDAO
```java
CompletableFuture<Boolean> addShip(Ship ship)
CompletableFuture<Ship> getShip(String shipId)
CompletableFuture<List<Ship>> getAllShips()
CompletableFuture<Boolean> updateShip(String shipId, Map<String, Object> updates)
CompletableFuture<Boolean> deleteShip(String shipId)
```

### RoomDAO
```java
CompletableFuture<Boolean> addRoom(Room room)
CompletableFuture<Room> getRoom(String id)
CompletableFuture<List<Room>> getAllRooms()
CompletableFuture<List<Room>> getRoomsByShip(String shipId)
CompletableFuture<Boolean> updateRoom(String id, Map<String, Object> updates)
CompletableFuture<Boolean> deleteRoom(String id)
```

### TourInstanceDAO
```java
CompletableFuture<Boolean> addTourInstance(TourInstance tourInstance)
CompletableFuture<TourInstance> getTourInstance(String id)
CompletableFuture<List<TourInstance>> getAllTourInstances()
CompletableFuture<List<TourInstance>> getTourInstancesByTour(String tourId)
CompletableFuture<Boolean> updateTourInstance(String id, Map<String, Object> updates)
CompletableFuture<Boolean> deleteTourInstance(String id)
```

### BookingDAO
```java
CompletableFuture<Boolean> addBooking(Booking booking)
CompletableFuture<Booking> getBooking(String id)
CompletableFuture<List<Booking>> getAllBookings()
CompletableFuture<List<Booking>> getBookingsByUser(String userId)
CompletableFuture<Boolean> updateBooking(String id, Map<String, Object> updates)
CompletableFuture<Boolean> deleteBooking(String id)
```

### FeaturedPhotoDAO
```java
CompletableFuture<Boolean> addPhoto(FeaturedPhoto photo)
CompletableFuture<FeaturedPhoto> getPhoto(String photoId)
CompletableFuture<List<FeaturedPhoto>> getAllPhotos()
CompletableFuture<Boolean> updatePhoto(String photoId, FeaturedPhoto photo)
CompletableFuture<Boolean> deletePhoto(String photoId)
```

## Tips for Migration

1. **Use Java Streams for List Processing**
   ```java
   tourDAO.getAllTours().thenAccept(tours -> {
       List<TourCard> cards = tours.stream()
           .map(TourCard::fromTour)
           .collect(Collectors.toList());
       updateRecyclerView(cards);
   });
   ```

2. **Chain Multiple Async Operations**
   ```java
   tourDAO.getTour(tourId)
       .thenCompose(tour -> {
           // Get related ships
           return shipDAO.getShip(tour.getShipId())
               .thenApply(ship -> new TourDetails(tour, ship));
       })
       .thenAccept(details -> {
           updateUI(details);
       });
   ```

3. **Handle Errors Consistently**
   ```java
   tourDAO.getAllTours()
       .thenAccept(tours -> updateUI(tours))
       .exceptionally(e -> {
           Log.e("TAG", "Failed to load tours", e);
           showErrorDialog(e.getMessage());
           return null;
       });
   ```

4. **Show Loading States**
   ```java
   showLoading(true);
   tourDAO.getAllTours()
       .thenAccept(tours -> {
           showLoading(false);
           updateUI(tours);
       })
       .exceptionally(e -> {
           showLoading(false);
           showError(e);
           return null;
       });
   ```

## Testing Checklist

- [ ] Create/Read/Update/Delete for each entity
- [ ] List views load data correctly
- [ ] Detail views show proper information
- [ ] Error messages display on failures
- [ ] Loading indicators appear while fetching
- [ ] No memory leaks from async operations
- [ ] Proper cleanup on fragment destroy

## Priority Order for Updates

1. **High Priority** (Core functionality)
   - [ ] AdminDashboardFragment
   - [ ] ViewBookingsFragment
   - [ ] CustomerListFragment

2. **Medium Priority** (Management features)
   - [ ] ManageShipsFragment
   - [ ] ManageToursFragment
   - [ ] ManageRoomsFragment

3. **Low Priority** (Secondary features)
   - [ ] AdminProfileFragment
   - [ ] ManageTourInstancesFragment
   - [ ] ManageFeaturedPhotosFragment

## Progress Tracking

```
Overall Progress: [████████░░] 80%

Completed:
✅ Auth layer (100%)
✅ DAO layer (100%)
✅ Dependencies (100%)
✅ Database schema (100%)

Remaining:
⏳ Admin activities/fragments (0%)
- 8 fragments to update
- 8 activities to update
```

## Resources

- Async patterns: `BUILD_FIX_SUMMARY.md`
- Database schema: `MIGRATION_GUIDE.md`
- Quick reference: `QUICKSTART.md`

Good luck with the migration! 🚀
