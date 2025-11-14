// ui/auction/AuctionGoingOn.java
package com.example.secondchance.ui.auction;

import java.util.Date;

public class AuctionGoingOn {
    private String productName;
    private String currentPrice;
    private int quantity;
    private String imageUrl; // hoặc int nếu dùng drawable
    private long endTimeMillis; // thời gian kết thúc (milliseconds)
    private String productId;
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    // Constructor
    public AuctionGoingOn(String productName, String currentPrice, int quantity, String imageUrl, long endTimeMillis, String productId) {
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.endTimeMillis = endTimeMillis;
        this.productId = productId;
    }

    // Getters
    public String getProductName() { return productName; }
    public String getCurrentPrice() { return currentPrice; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public long getEndTimeMillis() { return endTimeMillis; }

    // Setters (nếu cần)
    public void setProductName(String productName) { this.productName = productName; }
    public void setCurrentPrice(String currentPrice) { this.currentPrice = currentPrice; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setEndTimeMillis(long endTimeMillis) { this.endTimeMillis = endTimeMillis; }
}
