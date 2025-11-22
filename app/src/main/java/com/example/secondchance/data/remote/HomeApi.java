package com.example.secondchance.data.remote;

import com.example.secondchance.dto.request.BidRequest;
import com.example.secondchance.dto.response.AuctionListUserResponse;
import com.example.secondchance.dto.response.ProductDetailResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HomeApi {
  
  // GET /api/products/home
  @GET("/api/products/home")
  Call<HomeEnvelope> getHome();
  
  // GET /api/products/{id}
  @GET("/api/products/{id}")
  Call<ProductDetailResponse> getProductDetail(@Path("id") String productId);
  
  // GET /api/products/{id}
  @GET("/api/products/auctions/{id}")
  Call<AuctionListUserResponse> getProductAuctionUser(@Path("id") String productId);
  
  @POST("/api/products/auctions/{id}/bid")
  Call<Void> placeBid(@Path("id") String auctionId, @Body BidRequest body);
  
  class HomeEnvelope {
    @SerializedName("success") public boolean success;
    @SerializedName("data")    public Data data;
    public static class Data {
      @SerializedName("featuredAuction") public FeaturedAuction featuredAuction;
      @SerializedName("categories")      public List<Category> categories;
      @SerializedName("suggestions")     public Suggestions suggestions;
    }
  }
  
  public static class FeaturedAuction {
    @SerializedName("id")           public String id;
    @SerializedName("title")        public String title;
    @SerializedName("imageUrl")     public String imageUrl;
    @SerializedName("quantity")     public int quantity;
    @SerializedName("currentPrice") public long currentPrice;
    @SerializedName("currency")     public String currency;
    @SerializedName("endsAt")       public String endsAt; // ISO 8601
    @SerializedName("condition")    public String condition;
    @SerializedName("featured")     public boolean featured;
  }
  
  public static class Category {
    @SerializedName("id")    public String id;
    @SerializedName("name")  public String name;
    @SerializedName("icon")  public String icon;
    @SerializedName("order") public int order;
  }
  
  public static class Suggestions {
    @SerializedName("items")    public List<SuggestionItem> items;
    @SerializedName("page")     public int page;
    @SerializedName("pageSize") public int pageSize;
    @SerializedName("total")    public int total;
  }
  
  public static class SuggestionItem {
    @SerializedName("id")             public String id;
    @SerializedName("title")          public String title;
    @SerializedName("imageUrl")       public String imageUrl;
    @SerializedName("conditionLabel") public String conditionLabel;
    @SerializedName("quantity")       public int quantity;
    @SerializedName("endsInSec")      public long endsInSec;
    @SerializedName("currentPrice")   public long currentPrice;
    @SerializedName("currency")       public String currency;
  }
}
