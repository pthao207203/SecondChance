package com.example.secondchance.dto.response;

import java.util.List;

public class AuctionListUserResponse {
  public boolean success;
  public Data data;
  
  public static class Data {
    public String id;
    public String title;
    public String imageUrl;
    public int quantity;
    public long currentPrice;
    public String currency;
    public String endsAt;
    public String condition;
    public boolean featured;
    public String finalState;
    public int biddersCount;
    public List<Bid> bidHistory;
    public CurrentUser currentUser;
  }
  public static class Bid {
    public String userId;
    public long amount;
    public String createdAt;
    public User byUser;
  }
  public static class User {
    public String id;
    public String avatar;
    public String name;
  }
  public static class CurrentUser {
      public String id;
      public String avatar;
      public String name;

      public long balance;
      public Long myBidAmount;
      public boolean isLeading;

      public Long maxCanBid;
  }
}
