package com.example.secondchance.data.remote;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface CartApi {

    @GET("/api/me/cart")
    Call<CartEnvelope> getCart();

    @POST("/api/me/cart")
    Call<CartEnvelope> addToCart(@Body AddToCartRequest request);

    @DELETE("/api/me/cart/{itemId}")
    Call<CartEnvelope> removeFromCart(@Path("itemId") String itemId);

    @PUT("/api/me/cart/{itemId}")
    Call<CartEnvelope> updateCartItem(
            @Path("itemId") String itemId,
            @Body UpdateCartRequest request
    );

    class AddToCartRequest {
        public String productId;
        public int qty;

        public AddToCartRequest(String productId, int qty) {
            this.productId = productId;
            this.qty = qty;
        }
    }

    class UpdateCartRequest {
        public int qty;

        public UpdateCartRequest(int qty) {
            this.qty = qty;
        }
    }

    class CartEnvelope {
        public boolean success;
        public Data data;
        public ErrorResponse error;

        public static class Data {
            public List<CartItem> cart;
        }

        public static class ErrorResponse {
            public String message;
        }
    }

    class CartItem {
        @SerializedName("_id")
        public String id; // ID của item trong giỏ hàng
        public String productId; // ID của sản phẩm
        public int qty;
        public long price;
        public String addedAt;

        @SerializedName("product")
        public ProductInfo product;

        public transient boolean isSelected = false;

        public static class ProductInfo {
            public String id, title, description, imageUrl;
            public List<String> images;
        }

        // Helper methods
        public String getName() {
            return (product != null && product.title != null) ? product.title : "Đang tải...";
        }

        public String getDescription() {
            return (product != null && product.description != null) ? product.description : "";
        }

        public String getImageUrl() {
            if (product != null) {
                if (product.imageUrl != null) return product.imageUrl;
                if (product.images != null && !product.images.isEmpty()) return product.images.get(0);
            }
            return null;
        }

        public long getTotalPrice() {
            return price * qty;
        }
    }
}
