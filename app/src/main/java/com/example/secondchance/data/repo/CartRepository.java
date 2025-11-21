package com.example.secondchance.data.repo;

import android.util.Log;
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void removeFromCart(String productId, CartCallback callback) {
        RetrofitProvider.cart().removeFromCart(productId).enqueue(new Callback<CartApi.CartEnvelope>() {
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
                    } else if (!response.isSuccessful()) {
                        errorMsg = "Lỗi " + response.code() + ": Không thể xóa sản phẩm.";
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

    /**
     * Xóa nhiều sản phẩm khỏi giỏ hàng dựa trên danh sách productId
     * @param productIds Danh sách productId cần xóa
     * @param callback Callback trả về kết quả
     */
    public void removeMultipleFromCart(List<String> productIds, CartCallback callback) {
        if (productIds == null || productIds.isEmpty()) {
            callback.onSuccess(getCachedCart());
            return;
        }

        final List<String> mutableProductIds = new ArrayList<>(productIds);
        final List<String> errors = new ArrayList<>();

        removeNextItem(mutableProductIds, errors, callback);
    }

    private void removeNextItem(List<String> productIds, List<String> errors, CartCallback callback) {
        if (productIds.isEmpty()) {
            if (errors.isEmpty()) {
                fetchCart(callback);
            } else {
                Log.w(TAG, "Some items failed to delete: " + errors);
                fetchCart(new CartCallback() {
                    @Override
                    public void onSuccess(List<CartApi.CartItem> items) {
                        callback.onSuccess(items);
                    }

                    @Override
                    public void onError(String error) {
                        callback.onError("Lỗi khi làm mới giỏ hàng: " + error);
                    }
                });
            }
            return;
        }

        String productId = productIds.remove(0);
        RetrofitProvider.cart().removeFromCart(productId).enqueue(new Callback<CartApi.CartEnvelope>() {
            @Override
            public void onResponse(Call<CartApi.CartEnvelope> call, Response<CartApi.CartEnvelope> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().success) {
                    String errorMsg = "Xóa sản phẩm " + productId + " thất bại";
                    if (response.body() != null && response.body().error != null) {
                        errorMsg = response.body().error.message;
                    }
                    errors.add(errorMsg);
                }
                removeNextItem(productIds, errors, callback);
            }

            @Override
            public void onFailure(Call<CartApi.CartEnvelope> call, Throwable t) {
                Log.e(TAG, "removeFromCart error for " + productId, t);
                errors.add("Lỗi mạng khi xóa " + productId);
                removeNextItem(productIds, errors, callback);
            }
        });
    }


    public interface CartCallback {
        void onSuccess(List<CartApi.CartItem> items);
        void onError(String error);
    }
}
