package com.example.secondchance.dto.response;

import java.util.List;

public class AuctionListResponse {
  public boolean success;
  public Data data;

  public static class Data {
    public List<Item> items;
    public int page;
    public int pageSize;
    public int total;
    public CurrentUser currentUser; // Thêm field này để lấy ID user hiện tại
  }

  public static class Item {
    public String id;
    public String title;
    public String imageUrl;
    public int quantity;
    public long currentPrice;
    public String productId;
    public String currency;
    public String endsAt;   // ISO-8601
    public String condition;
    public boolean featured;
    public Long myBidAmount;
    public Integer myBidCount;
    public Boolean isLeading;
    public List<Bid> bidHistory; // Thêm field này để đếm ở client (nếu api trả về)
  }

  public static class CurrentUser {
      public String id;
      public String name;
      public String avatar;
      public long balance;
  }

  public static class Bid {
      public String userId;
      public long amount;
      public String createdAt;
  }
}
