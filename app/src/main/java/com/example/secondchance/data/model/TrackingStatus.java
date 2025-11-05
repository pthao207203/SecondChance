package com.example.secondchance.data.model;

public class TrackingStatus {
    private String timestamp;
    private String statusDescription;
    private boolean isActive;

    public TrackingStatus(String timestamp, String statusDescription, boolean isActive) {
        this.timestamp = timestamp;
        this.statusDescription = statusDescription;
        this.isActive = isActive;
    }

    public String getTimestamp() { return timestamp; }
    public String getStatusDescription() { return statusDescription; }
    public boolean isActive() { return isActive; }

    public void setActive(boolean active) {
        isActive = active;
    }
}