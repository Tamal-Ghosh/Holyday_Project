package com.example.shipvoyage.model;
import com.google.gson.annotations.SerializedName;

public class TourInstance {
    @SerializedName("id")
    public String id;
    
    @SerializedName("tour_id")
    public String tourId;
    
    @SerializedName("ship_id")
    public String shipId;
    
    @SerializedName("start_date")
    public String startDate;
    
    @SerializedName("end_date")
    public String endDate;
    
    // These fields are for display only, not sent to database
    public transient String tourName;
    public transient String shipName;
    public transient String fromLocation;
    public transient String toLocation;
    
    public TourInstance() {}
    public TourInstance(String id, String tourId, String shipId, String startDate, String endDate) {
        this.id = id;
        this.tourId = tourId;
        this.shipId = shipId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTourId() {
        return tourId;
    }
    public void setTourId(String tourId) {
        this.tourId = tourId;
    }
    public String getShipId() {
        return shipId;
    }
    public void setShipId(String shipId) {
        this.shipId = shipId;
    }
    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public String getTourName() {
        return tourName;
    }
    public void setTourName(String tourName) {
        this.tourName = tourName;
    }
    public String getShipName() {
        return shipName;
    }
    public void setShipName(String shipName) {
        this.shipName = shipName;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }

    @Override
    public String toString() {
        String tourPart = tourName != null && !tourName.isEmpty() ? tourName : "";
        String datePart = startDate != null && !startDate.isEmpty() ? startDate : "";
        if (!tourPart.isEmpty() && !datePart.isEmpty()) {
            return tourPart + " - " + datePart;
        }
        if (!tourPart.isEmpty()) return tourPart;
        if (!datePart.isEmpty()) return datePart;
        return id != null ? id : "";
    }
}
