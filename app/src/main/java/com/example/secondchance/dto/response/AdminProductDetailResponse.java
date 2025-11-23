package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdminProductDetailResponse {
  
  @SerializedName("success")
  public boolean success;
  
  @SerializedName("data")
  public AdminProduct data;
  
  public static class AdminProduct {
    
    @SerializedName("id")
    public String id;
    
    @SerializedName("name")
    public String name;
    
    @SerializedName("description")
    public String description;
    
    @SerializedName("media")
    public List<String> media;
    
    @SerializedName("price")
    public double price;
    
    @SerializedName("currency")
    public String currency;
    
    @SerializedName("quantity")
    public int quantity;
    
    @SerializedName("condition")
    public String condition;
    
    @SerializedName("sellerId")
    public String sellerId;
    
    @SerializedName("createdAt")
    public String createdAt;
    
    // ⭐ THÊM MỚI:
    @SerializedName("originProof")
    public OriginProof originProof;
    
    @SerializedName("originUrl")
    public String originUrl;
    
    @SerializedName("conditionNote")
    public String conditionNote;
    
    @SerializedName("newPercent")
    public Integer newPercent;
    
    @SerializedName("damagePercent")
    public Integer damagePercent;
    
    @SerializedName("warrantyMonths")
    public Integer warrantyMonths;
    
    @SerializedName("returnPolicy")
    public Boolean returnPolicy;
    
    @SerializedName("hasOrigin")
    public Boolean hasOrigin;
    
    @SerializedName("priceTypeLabel")
    public String priceTypeLabel;
    
    @SerializedName("seller")
    public Seller seller;
  }
  
  public static class OriginProof {
    @SerializedName("images")
    public List<String> images;
  }
  
  
  public static class Seller {
    @SerializedName("id")
    public String id;
    
    @SerializedName("userName")
    public String userName;
    
    @SerializedName("userAvatar")
    public String userAvatar;
    
    @SerializedName("shopName")
    public String shopName;
  }
}
