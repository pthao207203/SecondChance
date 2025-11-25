package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminProductListResponse {
  @SerializedName("success")
  public boolean success;
  
  @SerializedName("data")
  public AdminProductPage data;
  
  public class AdminProductPage {
    @SerializedName("items")
    public List<AdminProduct> items;
    
    @SerializedName("page")
    public int page;
    
    @SerializedName("pageSize")
    public int pageSize;
    
    @SerializedName("total")
    public int total;
  }
  
  public class AdminProduct {
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("createdAt")
    public String createdAt;
    
    @SerializedName("quantity")
    public int quantity;
    
    @SerializedName("price")
    public int price;
    
    @SerializedName("thumbnail")
    public String thumbnail;
    
    @SerializedName("priceType")
    public String priceType;
    
    @SerializedName("auctionEndsAt")
    public String auctionEndsAt;
    
  }
}
