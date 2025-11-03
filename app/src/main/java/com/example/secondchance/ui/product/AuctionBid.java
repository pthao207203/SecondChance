package com.example.secondchance.ui.product;

public class AuctionBid {
    private String userId;
    private String userName;
    private String userAvatar;
    private int bidNumber; // Lần đấu thứ mấy (1, 2, 3...)
    private double bidAmount; // Giá đấu
    private double increaseAmount; // Số tiền tăng so với lần trước
    private String bidTime; // "10:05:59, 06/08/2024"

    public AuctionBid() {}

    public AuctionBid(String userId, String userName, String userAvatar, int bidNumber,
                      double bidAmount, double increaseAmount, String bidTime) {
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.bidNumber = bidNumber;
        this.bidAmount = bidAmount;
        this.increaseAmount = increaseAmount;
        this.bidTime = bidTime;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserAvatar() { return userAvatar; }
    public int getBidNumber() { return bidNumber; }
    public double getBidAmount() { return bidAmount; }
    public double getIncreaseAmount() { return increaseAmount; }
    public String getBidTime() { return bidTime; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
    public void setBidNumber(int bidNumber) { this.bidNumber = bidNumber; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }
    public void setIncreaseAmount(double increaseAmount) { this.increaseAmount = increaseAmount; }
    public void setBidTime(String bidTime) { this.bidTime = bidTime; }
}