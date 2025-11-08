package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailResponse {
  public boolean success;
  public Data data;
  
  public static class Data {
    @SerializedName("id")            public String id;
    @SerializedName("name")          public String name;
    @SerializedName("description")   public String description;
    
    // Danh sách URL ảnh
    @SerializedName("media")         public List<String> media;
    
    // Giá: dùng long để đủ dải VND
    @SerializedName("price")         public long price;
    
    @SerializedName("currency")      public String currency;
    @SerializedName("quantity")      public int quantity;
    
    // Tình trạng BE trả "active"
    @SerializedName("condition")     public String condition;
    
    // Khóa ngoại
    @SerializedName("sellerId")      public String sellerId;
    @SerializedName("categoryId")    public String categoryId;
    @SerializedName("brandId")       public String brandId;
    
    // ISO8601
    @SerializedName("createdAt")     public String createdAt;
    
    // Thuộc tính phụ
    @SerializedName("usageTimeMonths") public Integer usageTimeMonths; // có thể null
    @SerializedName("reviewCount")     public Integer reviewCount;     // có thể null
    @SerializedName("conditionNote")   public String conditionNote;
    @SerializedName("newPercent")      public Integer newPercent;
    @SerializedName("damagePercent")   public Integer damagePercent;
    @SerializedName("warrantyMonths")  public Integer warrantyMonths;
    
    // Ảnh đại diện nhanh
    @SerializedName("thumbnail")     public String thumbnail;
    
    // Thông tin người bán (đã embed)
    @SerializedName("seller")        public Seller seller;
    @SerializedName("hasOrigin")
    public Boolean hasOrigin; // true/false, có thể null nếu BE không trả
    
    @SerializedName("originLink")
    public OriginLink originLink; // có thể null
    
    // Nên có constructor mặc định để tránh NPE
    public void ensureDefaults() {
      if (media == null) media = new ArrayList<>();
      if (hasOrigin == null) hasOrigin = false;
      // originLink có thể để null — tùy UI xử lý
    }
  }
  
  public static class Seller {
    public String id;
    @SerializedName("userAvatar")
    public String userAvatar;
    public String shopName;
    public FirstComment firstComment;
  }
  public static class FirstComment {
    public double rate;
    public String description;
    public List<String> media;
    public String createdAt;
    public User byUser;
  }
  public static class User {
    public String id;
    public String name;
    public String avatar;
  }
  public static class OriginLink {
    @SerializedName("description")
    public String description;
    @SerializedName("url")
    public List<String> url;
  }
}
