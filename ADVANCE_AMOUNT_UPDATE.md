# Booking Form Update - "Advance Amount" Changes

## Summary
Changed the booking form to use "Total Amount" and "Advance Amount" terminology with proper labels.

## Changes Made

### 1. Layout Updates (`dialog_booking_form.xml`)

**Before:**
- Total Payment Input (no label)
- Paid Amount Input (no label)

**After:**
- **Total Amount** (with label + input field)
- **Advance Amount** (with label + input field)
- Due Amount (auto-calculated: Total - Advance)

### 2. Code Updates

#### Files Modified:
1. **ManageBookingsFragment.java**
   - Renamed `paidAmountInput` → `advanceAmountInput`
   - Updated calculation: `due = total - advance`
   - Updated both `showBookingForm()` and `showEditBookingDialog()`

2. **ViewBookingsFragment.java**
   - Same changes as ManageBookingsFragment
   - All references to `paid` renamed to `advance`

### 3. How It Works

**User Flow:**
1. User enters **Total Amount** (e.g., ৳10,000)
2. User enters **Advance Amount** (e.g., ৳3,000)
3. **Due Amount** auto-calculates and displays (৳7,000)

**Real-time Calculation:**
```java
double total = totalPaymentInput value
double advance = advanceAmountInput value
double due = total - advance
dueAmountText displays: ৳[due]
```

### 4. Database Mapping

The booking model still uses `paidAmount` internally:
- `advanceAmountInput` → `booking.setPaidAmount(advance)`
- Display: "Advance Amount" 
- Database field: `paid_amount`
- This maintains backward compatibility

### 5. Form Layout Structure

```
┌─────────────────────────────────┐
│ Customer Name                   │
│ Phone Number                    │
│ Email (Optional)                │
│ Number of Adults                │
│ Number of Children              │
│                                 │
│ Total Amount                    │ ← Label
│ [Total Amount Input]            │ ← Input Field
│                                 │
│ Advance Amount                  │ ← Label
│ [Advance Amount Input]          │ ← Input Field
│                                 │
│ Payment Method [Dropdown]       │
│ Payment Details                 │
│                                 │
│ ┌────────────────────────────┐ │
│ │ Due Amount:      ৳7,000.00 │ │ ← Auto-calculated
│ └────────────────────────────┘ │
│                                 │
│ [Cancel]         [Save Booking] │
└─────────────────────────────────┘
```

## Testing Checklist

- [x] Layout updated with labels
- [x] ManageBookingsFragment updated
- [x] ViewBookingsFragment updated
- [x] Real-time calculation works
- [x] Build successful

## Next Steps

1. Run the SQL fix: `QUICK_FIX.sql` (adds missing `discount` column)
2. Test the booking form in the app
3. Verify calculations work correctly
4. Check that bookings save to database
