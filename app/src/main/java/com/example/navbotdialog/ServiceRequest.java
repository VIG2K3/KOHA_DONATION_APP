package com.example.navbotdialog;

import java.io.Serializable;

public class ServiceRequest implements Serializable{

    private String addItems;
    private String imageUrl;
    private String pickUpLocation;
    private String pickUpTime;
    private String status;
    private long timestamp;
    private String userId;

    public ServiceRequest() {
        // Required empty constructor for Firebase
    }

    public String getAddItems() { return addItems; }
    public String getImageUrl() { return imageUrl; }
    public String getPickUpLocation() { return pickUpLocation; }
    public String getPickUpTime() { return pickUpTime; }
    public String getStatus() { return status; }
    public long getTimestamp() { return timestamp; }
    public String getUserId() { return userId; }
}

