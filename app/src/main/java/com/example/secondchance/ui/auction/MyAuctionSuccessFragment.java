
// ============================================
// MyAuctionSuccessFragment.java
// ============================================
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentRecyclerCardBinding;
import com.example.secondchance.dto.response.AuctionListResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyAuctionSuccessFragment extends Fragment {
    private static final String TAG = "MyAuctionSuccess";
    private FragmentRecyclerCardBinding binding;
    private AuctionSuccessAdapter adapter;
    private List<AuctionGoingOn> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AuctionSuccessAdapter(items);
        binding.recyclerView.setAdapter(adapter);

        loadData();
    }

    private void loadData() {
        ProductApi api = RetrofitProvider.product();

        api.getSuccessfulAuctions(1, 20).enqueue(new Callback<AuctionListResponse>() {
            @Override
            public void onResponse(Call<AuctionListResponse> call, Response<AuctionListResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().success) {
                    String errorMsg = "Không thể tải danh sách đấu giá thành công";
                    if (response.code() == 401) {
                        errorMsg = "Vui lòng đăng nhập để xem đấu giá của bạn";
                    }
                    showError(errorMsg);
                    return;
                }

                items.clear();
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

                for (AuctionListResponse.Item item : response.body().data.items) {
                    String productId = item.productId != null ? item.productId : item.id;

                    items.add(new AuctionGoingOn(
                            item.title,
                            currencyFormat.format(item.currentPrice),
                            item.quantity,
                            item.imageUrl,
                            System.currentTimeMillis() - 1000, // Already ended
                            productId
                    ));
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }

                // Automatically place orders for successful auctions
                if (response.body().data.items != null && !response.body().data.items.isEmpty()) {
                    autoPlaceOrders(response.body().data.items);
                }
            }

            @Override
            public void onFailure(Call<AuctionListResponse> call, Throwable t) {
                Log.e(TAG, "Load error", t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void autoPlaceOrders(List<AuctionListResponse.Item> items) {
        OrderApi orderApi = RetrofitProvider.order();

        for (AuctionListResponse.Item item : items) {
            String productId = item.productId != null ? item.productId : item.id;
            Log.d(TAG, "Attempting to auto-order for product: " + productId);

            // 1. Prepare preview request to get default address
            List<OrderApi.CartItemInfo> cartItems = new ArrayList<>();
            OrderApi.CartItemInfo cItem = new OrderApi.CartItemInfo();
            cItem.productId = productId;
            cItem.qty = item.quantity > 0 ? item.quantity : 1;
            cartItems.add(cItem);

            OrderApi.PreviewRequestBody previewBody = new OrderApi.PreviewRequestBody();
            previewBody.items = cartItems;

            // 2. Call Preview
            orderApi.previewOrder(previewBody).enqueue(new Callback<OrderApi.OrderPreviewResponse>() {
                @Override
                public void onResponse(Call<OrderApi.OrderPreviewResponse> call, Response<OrderApi.OrderPreviewResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                        placeOrderForItem(item, response.body().data, productId);
                    } else {
                        Log.e(TAG, "Preview failed for item " + item.title + " Code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<OrderApi.OrderPreviewResponse> call, Throwable t) {
                    Log.e(TAG, "Preview error for item " + item.title, t);
                }
            });
        }
    }

    private void placeOrderForItem(AuctionListResponse.Item item, OrderApi.PreviewData data, String productId) {
        String defaultAddressId = null;
        if (data.addresses != null) {
            for (OrderApi.Address addr : data.addresses) {
                if (addr.isDefault) {
                    defaultAddressId = addr.id;
                    break;
                }
            }
            // Fallback to first address if no default
            if (defaultAddressId == null && !data.addresses.isEmpty()) {
                defaultAddressId = data.addresses.get(0).id;
            }
        }

        String paymentMethod = "COD"; // Default
        if (data.paymentMethods != null && !data.paymentMethods.isEmpty()) {
            // Prefer Wallet if available and balance sufficient? Or just pick first.
            // User requested "default". First one is usually good default.
            paymentMethod = data.paymentMethods.get(0).code;
        }

        if (defaultAddressId == null) {
            Log.e(TAG, "No address found for auto order: " + item.title);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Không tìm thấy địa chỉ để đặt hàng: " + item.title, Toast.LENGTH_SHORT).show()
                );
            }
            return;
        }

        OrderApi.PlaceOrderRequestBody body = new OrderApi.PlaceOrderRequestBody();
        body.items = new ArrayList<>();
        OrderApi.CartItemInfo cItem = new OrderApi.CartItemInfo();
        cItem.productId = productId;
        cItem.qty = item.quantity > 0 ? item.quantity : 1;
        body.items.add(cItem);

        body.paymentMethod = paymentMethod;
        body.shippingAddressId = defaultAddressId;
        body.shippingFee = 0; // Server should calculate or we take from preview logic if available
        body.note = "Auto placed successful auction";

        Log.d(TAG, "Placing order for " + item.title + " with Addr: " + defaultAddressId);

        RetrofitProvider.order().placeOrder(body).enqueue(new Callback<OrderApi.PlaceOrderResponse>() {
            @Override
            public void onResponse(Call<OrderApi.PlaceOrderResponse> call, Response<OrderApi.PlaceOrderResponse> res) {
                if (res.isSuccessful() && res.body() != null && res.body().success) {
                    Log.d(TAG, "Auto placed order success for " + item.title);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Đã tự động đặt hàng: " + item.title, Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "Place order failed: " + res.code() + " Msg: " + res.message());
                    if (getActivity() != null) {
                         getActivity().runOnUiThread(() ->
                             Toast.makeText(getContext(), "Lỗi đặt hàng: " + item.title, Toast.LENGTH_SHORT).show()
                         );
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderApi.PlaceOrderResponse> call, Throwable t) {
                Log.e(TAG, "Place order error", t);
            }
        });
    }

    private void showError(String message) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() ->
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
