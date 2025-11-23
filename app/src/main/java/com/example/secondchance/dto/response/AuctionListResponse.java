package com.example.secondchance.dto.response;

public class AuctionListResponse {
  public boolean success;
  public Data data;
  public static class Data {
    public java.util.List<Item> items;
    public int page;
    public int pageSize;
    public int total;
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
  }
}
