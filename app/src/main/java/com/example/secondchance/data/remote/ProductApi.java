package com.example.secondchance.data.remote;

import com.example.secondchance.dto.response.AdminProductDetailResponse;
import com.example.secondchance.dto.response.AdminProductListResponse;
import com.example.secondchance.dto.response.AuctionListResponse;
import com.example.secondchance.dto.request.ProductCreateRequest;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.ProductMetaResponse;
import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface ProductApi {

    @GET("/api/products/{id}")
    Call<ProductEnvelope> getProductById(@Path("id") String productId);

    @GET("/api/products/auctions")
    Call<AuctionListResponse> getAuctions(
      @Query("page") Integer page,
      @Query("pageSize") Integer pageSize
    );

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
}
