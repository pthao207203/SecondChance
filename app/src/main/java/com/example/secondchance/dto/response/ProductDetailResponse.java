package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailResponse {
  public boolean success;
  public Data data;
  public Data getData() {
    return data;
  }

  public static class Data {
    @SerializedName("id")            public String id;
    @SerializedName("name")          public String name;
    @SerializedName("description")   public String description;

    @SerializedName("media")         public List<String> media;

    @SerializedName("price")         public long price;
    public int priceType;
    public String auctionEndsAt;
    @SerializedName("currency")      public String currency;
    @SerializedName("quantity")      public int quantity;

    @SerializedName("condition")     public String condition;

    @SerializedName("sellerId")      public String sellerId;
    @SerializedName("categoryId")    public String categoryId;
    @SerializedName("brandId")       public String brandId;

    @SerializedName("createdAt")     public String createdAt;

    @SerializedName("usageTimeMonths") public Integer usageTimeMonths;
    @SerializedName("reviewCount")     public Integer reviewCount;
    @SerializedName("conditionNote")   public String conditionNote;
    @SerializedName("newPercent")      public Integer newPercent;
    @SerializedName("damagePercent")   public Integer damagePercent;
    @SerializedName("warrantyMonths")  public Integer warrantyMonths;

    @SerializedName("thumbnail")     public String thumbnail;

    @SerializedName("seller")        public Seller seller;
    @SerializedName("hasOrigin")
    public Boolean hasOrigin;

    @SerializedName("originLink")
    public OriginLink originLink;

    public void ensureDefaults() {
      if (media == null) media = new ArrayList<>();
      if (hasOrigin == null) hasOrigin = false;

    }
    public long getPrice() {
      return price;
    }
  }

  public static class Seller {
    public String id;
    @SerializedName("userAvatar")
    public String userAvatar;
    public String shopName;
    public double userRate;
    public String shopAddress;
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
