package com.example.shipvoyage.model;
import com.google.gson.annotations.SerializedName;

public class Booking {
    @SerializedName("id")
    public String id;
    
    @SerializedName("tour_instance_id")
    public String tourInstanceId;
    
    @SerializedName("room_id")
    public String roomId;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("phone")
    public String phone;
    
    @SerializedName("email")
    public String email;
    
    @SerializedName("payment_method")
    public String paymentMethod;

    @SerializedName("payment_details")
    public String paymentDetails;
    
    @SerializedName("total_payment")
    public double totalPayment;
    
    @SerializedName("paid_amount")
    public double paidAmount;
    
    @SerializedName("due_amount")
    public double dueAmount;
    
    @SerializedName("discount")
    public double discount;
    
    @SerializedName("adult_count")
    public int adultCount;
    
    @SerializedName("child_count")
    public int childCount;
    
    @SerializedName("status")
    public String status;
    
    // Display fields (not stored in DB)
    public transient String roomName;
    public transient String roomType;
    public transient String tourName;
    
    // Legacy fields for backward compatibility with existing adapters
    public transient String userId;
    public transient String customerName;
    public transient String customerEmail;
    public transient String customerPhone;
    public transient java.util.List<String> selectedRooms;
    public transient double price;
    public transient String fromLocation;
    public transient String toLocation;
    public transient String departureDate;
    public transient String returnDate;
    
    public Booking() {}
    
    public Booking(String id, String tourInstanceId, String roomId, String name, 
                   String phone, String email, String paymentMethod, String paymentDetails,
                   double totalPayment, double paidAmount, double dueAmount, double discount,
                   int adultCount, int childCount) {
        this.id = id;
        this.tourInstanceId = tourInstanceId;
        this.roomId = roomId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.paymentMethod = paymentMethod;
        this.paymentDetails = paymentDetails;
        this.totalPayment = totalPayment;
        this.paidAmount = paidAmount;
        this.dueAmount = dueAmount;
        this.discount = discount;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.status = "PENDING";
        // Sync legacy fields
        this.customerName = name;
        this.customerEmail = email;
        this.customerPhone = phone;
        this.price = totalPayment;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTourInstanceId() { return tourInstanceId; }
    public void setTourInstanceId(String tourInstanceId) { this.tourInstanceId = tourInstanceId; }
    
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }
    
    public double getTotalPayment() { return totalPayment; }
    public void setTotalPayment(double totalPayment) { this.totalPayment = totalPayment; }
    
    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }
    
    public double getDueAmount() { return dueAmount; }
    public void setDueAmount(double dueAmount) { this.dueAmount = dueAmount; }
    
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    
    public int getAdultCount() { return adultCount; }
    public void setAdultCount(int adultCount) { this.adultCount = adultCount; }
    
    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    
    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }
    
    // Legacy getters/setters for backward compatibility
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getCustomerName() { return customerName != null ? customerName : name; }
    public void setCustomerName(String customerName) { 
        this.customerName = customerName; 
        this.name = customerName;
    }
    
    public String getCustomerEmail() { return customerEmail != null ? customerEmail : email; }
    public void setCustomerEmail(String customerEmail) { 
        this.customerEmail = customerEmail;
        this.email = customerEmail;
    }
    
    public String getCustomerPhone() { return customerPhone != null ? customerPhone : phone; }
    public void setCustomerPhone(String customerPhone) { 
        this.customerPhone = customerPhone;
        this.phone = customerPhone;
    }
    
    public java.util.List<String> getSelectedRooms() { return selectedRooms; }
    public void setSelectedRooms(java.util.List<String> selectedRooms) { this.selectedRooms = selectedRooms; }
    
    public String getSelectedRoomsString() {
        if (selectedRooms == null || selectedRooms.isEmpty()) {
            return roomId != null ? roomId : "N/A";
        }
        return String.join(", ", selectedRooms);
    }
    
    public double getPrice() { return price > 0 ? price : totalPayment; }
    public void setPrice(double price) { 
        this.price = price;
        this.totalPayment = price;
    }
    
    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }
    
    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }
    
    public String getDepartureDate() { return departureDate; }
    public void setDepartureDate(String departureDate) { this.departureDate = departureDate; }
    
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    
    @Override
    public String toString() {
        return name != null ? name : "";
    }
}
