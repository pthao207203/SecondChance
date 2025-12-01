package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductListResponse {
  @SerializedName("success")
  public boolean success;
  
  @SerializedName("data")
  public Data data;
  
  public static class Data {
    @SerializedName("items")
    public List<Item> items;
    
    @SerializedName("page")
    public int page;
    
    @SerializedName("pageSize")
    public int pageSize;
    
    @SerializedName("total")
    public int total;
    
    @SerializedName("categories")
    public List<Category> categories;
  }
  
  public static class Item {
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("price")
    public long price;
    
    @SerializedName("priceType")
    public int priceType;
    
    @SerializedName("thumbnail")
    public String thumbnail;
    
    @SerializedName("quantity")
    public int quantity;
    
    @SerializedName("categoryId")
    public String categoryId;
    
    @SerializedName("brandId")
    public String brandId;
    
    @SerializedName("seller")
    public Seller seller;
  }
  
  public static class Seller {
    @SerializedName("id")
    public String id;
    
    @SerializedName("pickupCity")
    public String pickupCity;
    
    @SerializedName("userRate")
    public float userRate;
  }
  
  public static class Category {
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("icon")
    public String icon;
  }
}
