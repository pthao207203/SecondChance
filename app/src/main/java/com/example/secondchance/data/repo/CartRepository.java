package com.example.secondchance.data.repo;

import android.util.Log;
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class CartRepository {
    private static final String TAG = "CartRepository";
    private static CartRepository instance;
    private final List<CartApi.CartItem> localCartCache = new ArrayList<>();

    private CartRepository() {}

    public static synchronized CartRepository getInstance() {
        if (instance == null) {
            instance = new CartRepository();
        }
        return instance;
    }

    private void updateCache(List<CartApi.CartItem> serverCart) {
        localCartCache.clear();
        if (serverCart != null) {
            localCartCache.addAll(serverCart);
        }
    }

    public List<CartApi.CartItem> getCachedCart() {
        return new ArrayList<>(localCartCache);
    }
    
    public boolean isProductInCart(String productId) {
        if (productId == null || productId.isEmpty()) {
            return false;
        }
        for (CartApi.CartItem item : localCartCache) {
            if (productId.equals(item.productId)) {
                return true;
            }
        }
        return false;
    }

    public void fetchCart(CartCallback callback) {
        RetrofitProvider.cart().getCart().enqueue(new Callback<CartApi.CartEnvelope>() {
            @Override
            public void onResponse(Call<CartApi.CartEnvelope> call, Response<CartApi.CartEnvelope> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<CartApi.CartItem> serverCart = response.body().data != null ? response.body().data.cart : new ArrayList<>();
                    updateCache(serverCart);
                    callback.onSuccess(getCachedCart());
                } else {
                    String errorMsg = "Lỗi không xác định khi tải giỏ hàng";
                    if (response.body() != null && response.body().error != null) {
                        errorMsg = response.body().error.message;
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CartApi.CartEnvelope> call, Throwable t) {
                Log.e(TAG, "fetchCart error", t);
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void addToCart(String productId, int qty, CartCallback callback) {
        CartApi.AddToCartRequest request = new CartApi.AddToCartRequest(productId, qty);
        RetrofitProvider.cart().addToCart(request).enqueue(new Callback<CartApi.CartEnvelope>() {
            @Override
            public void onResponse(Call<CartApi.CartEnvelope> call, Response<CartApi.CartEnvelope> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<CartApi.CartItem> serverCart = response.body().data != null ? response.body().data.cart : new ArrayList<>();
                    updateCache(serverCart);
                    callback.onSuccess(getCachedCart());
                } else {
                     String errorMsg = "Thêm vào giỏ hàng thất bại";
                    if (response.body() != null && response.body().error != null) {
                        errorMsg = response.body().error.message;
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CartApi.CartEnvelope> call, Throwable t) {
                Log.e(TAG, "addToCart error", t);
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void removeFromCart(String itemId, CartCallback callback) {
        RetrofitProvider.cart().removeFromCart(itemId).enqueue(new Callback<CartApi.CartEnvelope>() {
            @Override
            public void onResponse(Call<CartApi.CartEnvelope> call, Response<CartApi.CartEnvelope> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<CartApi.CartItem> serverCart = response.body().data != null ? response.body().data.cart : new ArrayList<>();
                    updateCache(serverCart);
                    callback.onSuccess(getCachedCart());
                } else {
                    String errorMsg = "Xóa sản phẩm thất bại";
                    if (response.body() != null && response.body().error != null) {
                        errorMsg = response.body().error.message;
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CartApi.CartEnvelope> call, Throwable t) {
                Log.e(TAG, "removeFromCart error", t);
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }
    
    public interface CartCallback {
        void onSuccess(List<CartApi.CartItem> items);
        void onError(String error);
    }
}
