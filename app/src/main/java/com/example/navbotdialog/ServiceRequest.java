package com.example.navbotdialog;

import java.io.Serializable;

public class ServiceRequest implements java.io.Serializable {
    private String userId;
    private String addItems;
    private String pickUpLocation;
    private String pickUpTime;
    private String status;
    private String imageUrl;
    private long timestamp;

    // Firebase key (not stored in DB, just used locally)
    private transient String key;

    public ServiceRequest() {}

    // --- Getters and setters ---
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAddItems() { return addItems; }
    public void setAddItems(String addItems) { this.addItems = addItems; }

    public String getPickUpLocation() { return pickUpLocation; }
    public void setPickUpLocation(String pickUpLocation) { this.pickUpLocation = pickUpLocation; }

    public String getPickUpTime() { return pickUpTime; }          // <-- ADD THIS
    public void setPickUpTime(String pickUpTime) { this.pickUpTime = pickUpTime; } // <-- ADD THIS

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}

