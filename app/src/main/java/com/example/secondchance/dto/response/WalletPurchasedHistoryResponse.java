package com.example.secondchance.dto.response;

import java.util.List;

public class WalletPurchasedHistoryResponse {
  public boolean success;
  public Data data;
  
  public static class Data {
    public int page;
    public int pageSize;
    public int total;
    public List<Item> items;
  }
  
  public static class Item {
    public String orderId;
    public String time;      // ISO8601, ví dụ "2025-11-05T14:14:05.996Z"
    public long amount;      // VND
    public FirstProduct firstProduct;
  }
  
  public static class FirstProduct {
    public String id;
    public String name;
    public int qty;
    public long price;
    public String image;
  }
}
