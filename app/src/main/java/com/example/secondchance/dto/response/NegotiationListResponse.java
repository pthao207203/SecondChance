package com.example.secondchance.dto.response;

import java.util.List;

public class NegotiationListResponse {
  public boolean success;
  public Data data;
  
  public static class Data {
    public int page;
    public int pageSize;
    public int total;
    public List<Item> items;
  }
  
  public static class Item {
    public String id;
    public String productId;
    public String productName;
    public String productImage;
    public long offeredPrice;
    public int quantity;
    public int status;
    public String createdAt;
    public String acceptedAt;
    public int attemptNumber;
    public boolean isBuyer;
    public CurrentUser currentUser;
    public Counterpart counterpart;
  }
  
  public static class CurrentUser {
    public String id;
    public String name;
    public String avatar;
  }
  public static class Counterpart {
    public String id;
    public String name;
    public String avatar;
  }
}
