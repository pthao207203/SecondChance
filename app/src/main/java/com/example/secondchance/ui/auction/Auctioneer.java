// ui/auction/Auctioneer.java
package com.example.secondchance.ui.auction;

public class Auctioneer {
    private String bidderName;
    private String bidTime; // "10:05:59, 06/08/2024"
    private String bidAmount; // "1.000.000"
    private String priceDiff; // "(+25.000)"
    private String bidRound; // "Trả giá lần 1"
    private int avatarResId; // hoặc String nếu dùng URL

    // Constructor
    public Auctioneer(String bidderName, String bidTime, String bidAmount, String priceDiff, String bidRound, int avatarResId) {
        this.bidderName = bidderName;
        this.bidTime = bidTime;
        this.bidAmount = bidAmount;
        this.priceDiff = priceDiff;
        this.bidRound = bidRound;
        this.avatarResId = avatarResId;
    }

    // Getters
    public String getBidderName() { return bidderName; }
    public String getBidTime() { return bidTime; }
    public String getBidAmount() { return bidAmount; }
    public String getPriceDiff() { return priceDiff; }
    public String getBidRound() { return bidRound; }
    public int getAvatarResId() { return avatarResId; }
}
