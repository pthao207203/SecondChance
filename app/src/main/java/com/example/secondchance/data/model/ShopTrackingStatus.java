package com.example.secondchance.data.model;

public class ShopTrackingStatus {
    private String timestamp;
    private String statusDescription;
    private boolean isActive;

    public ShopTrackingStatus(String timestamp, String statusDescription, boolean isActive) {
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
