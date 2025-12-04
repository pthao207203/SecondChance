// data/remote/ProductApi.java
package com.example.secondchance.data.remote;

import com.example.secondchance.dto.response.AdminProductDetailResponse;
import com.example.secondchance.dto.response.AdminProductListResponse;
import com.example.secondchance.dto.response.AuctionListResponse;
import com.example.secondchance.dto.request.ProductCreateRequest;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.ProductListResponse;
import com.example.secondchance.dto.response.ProductMetaResponse;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PATCH;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface ProductApi {

    // IMPORTANT: Keep /api/ prefix to match RetrofitProvider base URL pattern
    // RetrofitProvider base: "http://10.0.2.2:3000/api/"
    // When path starts with /, it replaces the base path entirely

    @GET("/api/products/{id}")
    Call<ProductEnvelope> getProductById(@Path("id") String productId);
    
    @GET("/api/products")
    Call<ProductListResponse> getProducts(
      @Query("page") Integer page,
      @Query("pageSize") Integer pageSize,
      @Query("name") String name,
      @Query("city") String pickupCity,
      @Query("rating") Integer rating,
      @Query("status") Integer status,
      @Query("minPrice") Integer minPrice,
      @Query("maxPrice") Integer maxPrice,
      @Query("priceType") Integer priceType,
      @Query("categoryId") String categoryId
    );
    
    @GET("/api/products/auctions")
    Call<AuctionListResponse> getAuctions(
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize
    );

    // ============================================
    // MY AUCTIONS - NEW ENDPOINTS
    // ============================================

    @GET("/api/products/auctions/participated")
    Call<AuctionListResponse> getParticipatedAuctions(
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize
    );

    @GET("/api/products/auctions/successful")
    Call<AuctionListResponse> getSuccessfulAuctions(
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize
    );

    @GET("/api/products/auctions/failed")
    Call<AuctionListResponse> getFailedAuctions(
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize
    );

    @GET("/api/products/auctions/{id}")
    Call<AuctionDetailEnvelope> getAuctionById(@Path("id") String auctionId);

    @POST("/api/products/auctions/{id}/bid")
    Call<BasicResponse> bidAuction(
            @Path("id") String auctionId,
            @Body BidRequest body
    );

    // ============================================
    // ADMIN ENDPOINTS
    // ============================================

    @POST("/admin/products")
    Call<BasicResponse> createProduct(
            @Body ProductCreateRequest body
    );

    @GET("/admin/products/meta")
    Call<ProductMetaResponse> getProductMeta();

    @GET("/admin/products")
    Call<AdminProductListResponse> getAdminProducts(
            @Query("priceType") Integer priceType,
            @Query("deleted") boolean deleted,
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize
    );

    @GET("/admin/products/{id}")
    Call<AdminProductDetailResponse> getAdminProductById(@Path("id") String id);

    // ============================================
    // ORDER SHIPPING UPDATE
    // ============================================

    @PATCH("/api/orders/{id}/shipping")
    Call<BasicResponse> updateOrderShipping(
            @Path("id") String orderId,
            @Body UpdateShippingRequest body
    );

    // ============================================
    // REQUEST/RESPONSE CLASSES
    // ============================================

    class BidRequest {
        @SerializedName("amount")
        public long amount;

        public BidRequest(long amount) {
            this.amount = amount;
        }
    }

    class UpdateShippingRequest {
        @SerializedName("shippingAddressId")
        public String shippingAddressId;

        @SerializedName("shippingAddress")
        public ShippingAddress shippingAddress;

        public UpdateShippingRequest(String shippingAddressId) {
            this.shippingAddressId = shippingAddressId;
        }

        public UpdateShippingRequest(ShippingAddress shippingAddress) {
            this.shippingAddress = shippingAddress;
        }

        public static class ShippingAddress {
            @SerializedName("fullName")
            public String fullName;

            @SerializedName("phone")
            public String phone;

            @SerializedName("address")
            public String address;

            @SerializedName("city")
            public String city;

            @SerializedName("district")
            public String district;

            @SerializedName("ward")
            public String ward;
        }
    }

    class ProductEnvelope {
        @SerializedName("success")
        public boolean success;

        @SerializedName("data")
        public Product data;
    }

    class Product {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;

        @SerializedName("media")
        public List<String> media;

        @SerializedName("price")
        public int price;
    }

    class AuctionDetailEnvelope {
        @SerializedName("success")
        public boolean success;

        @SerializedName("data")
        public AuctionDetail data;

        public static class AuctionDetail {
            @SerializedName("id")
            public String id;

            @SerializedName("title")
            public String title;

            @SerializedName("imageUrl")
            public String imageUrl;

            @SerializedName("quantity")
            public int quantity;

            @SerializedName("currentPrice")
            public long currentPrice;

            @SerializedName("currency")
            public String currency;

            @SerializedName("endsAt")
            public String endsAt;

            @SerializedName("condition")
            public String condition;

            @SerializedName("featured")
            public boolean featured;

            @SerializedName("biddersCount")
            public int biddersCount;

            @SerializedName("bidHistory")
            public List<BidHistory> bidHistory;

            @SerializedName("currentUser")
            public CurrentUser currentUser;
        }

        public static class BidHistory {
            @SerializedName("userId")
            public String userId;

            @SerializedName("amount")
            public long amount;

            @SerializedName("createdAt")
            public String createdAt;

            @SerializedName("byUser")
            public User byUser;
        }

        public static class User {
            @SerializedName("id")
            public String id;

            @SerializedName("avatar")
            public String avatar;

            @SerializedName("name")
            public String name;
        }

        public static class CurrentUser {
            @SerializedName("id")
            public String id;

            @SerializedName("avatar")
            public String avatar;

            @SerializedName("balance")
            public long balance;

            @SerializedName("myBidAmount")
            public Long myBidAmount;
        }
    }
}