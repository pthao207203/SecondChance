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
    private int userBidCount;
    private String userLastBidPrice;
    
    public void setProductId(String productId) {
        this.productId = productId;
    }
    
    public String getProductId() {
        return productId;
    }
    
    // Constructor
    public AuctionGoingOn(String productName, String currentPrice, int quantity, String imageUrl, long endTimeMillis, String productId, int userBidCount, String userLastBidPrice) {
        this.productName = productName;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.endTimeMillis = endTimeMillis;
        this.productId = productId;
        this.userBidCount = userBidCount;
        this.userLastBidPrice = userLastBidPrice;
    }
    
    // Constructor overload for backward compatibility if needed, or just update usages. 
    // I will assume I might need to update usages or provide a default.
    // For now, I'll keep the old constructor? No, cleaner to update it if possible, but I don't see where it is instantiated.
    // The prompt says "bổ sung", implies adding to existing.
    // I will add setters/getters and keep a constructor that fits or update it. 
    // Since I don't see the instantiation code (likely in a Fragment/ViewModel), I'll add a constructor that calls this one or defaults.
    
    public AuctionGoingOn(String productName, String currentPrice, int quantity, String imageUrl, long endTimeMillis, String productId) {
        this(productName, currentPrice, quantity, imageUrl, endTimeMillis, productId, 0, "");
    }

    // Getters
    public String getProductName() { return productName; }
    public String getCurrentPrice() { return currentPrice; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public long getEndTimeMillis() { return endTimeMillis; }
    public int getUserBidCount() { return userBidCount; }
    public String getUserLastBidPrice() { return userLastBidPrice; }

    // Setters (nếu cần)
    public void setProductName(String productName) { this.productName = productName; }
    public void setCurrentPrice(String currentPrice) { this.currentPrice = currentPrice; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setEndTimeMillis(long endTimeMillis) { this.endTimeMillis = endTimeMillis; }
    public void setUserBidCount(int userBidCount) { this.userBidCount = userBidCount; }
    public void setUserLastBidPrice(String userLastBidPrice) { this.userLastBidPrice = userLastBidPrice; }
}
