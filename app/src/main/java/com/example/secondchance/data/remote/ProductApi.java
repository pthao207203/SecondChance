// data/remote/ProductApi.java
package com.example.secondchance.data.remote;

import com.google.gson.annotations.SerializedName;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface ProductApi {

    @GET("products/{id}")
    Call<ProductEnvelope> getProductById(@Path("id") String productId);

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
