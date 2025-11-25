package com.example.secondchance.ui.order;

import android.annotation.SuppressLint;
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
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentCanceledOrderDetailBinding;
import com.example.secondchance.R;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CanceledOrderDetailFragment extends Fragment {
    private static final String TAG = "CanceledDetailFrag";
    private FragmentCanceledOrderDetailBinding binding;
    private String receivedOrderId;
    private OrderItemAdapter productAdapter;
    private final List<OrderItem> productList = new ArrayList<>();
    private OrderApi orderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCanceledOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderApi = RetrofitProvider.order();

        setupProductRecyclerView();

        if (receivedOrderId != null && !receivedOrderId.isEmpty()) {
            loadCanceledOrderDetails(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null.");
            Toast.makeText(getContext(), "Không tìm thấy thông tin đơn hàng.", Toast.LENGTH_SHORT).show();
            binding.btnBuyAgain.setEnabled(false);
        }

        binding.btnBuyAgain.setOnClickListener(v -> {

            Toast.makeText(getContext(), "Tính năng Mua Lại đang phát triển...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupProductRecyclerView() {
        productAdapter = new OrderItemAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void loadCanceledOrderDetails(String orderId) {
        orderApi.getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call,
                                   @NonNull Response<OrderDetailResponse> resp) {
                if (!isAdded()) return;

                if (!resp.isSuccessful() || resp.body() == null || resp.body().data == null) {
                    Toast.makeText(getContext(), "Không tải được chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindOrderDetailToViews(resp.body().data);
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void bindOrderDetailToViews(OrderDetailResponse.Data data) {
        try {
            if (data.order == null) return;

            productList.clear();
            if (data.order.orderItems != null) {
                for (OrderDetailResponse.OrderItem dtoItem : data.order.orderItems) {

                    OrderItem modelItem = new OrderItem();
                    modelItem.name = dtoItem.name;
                    modelItem.imageUrl = dtoItem.imageUrl;
                    modelItem.price = dtoItem.price;
                    modelItem.quantity = dtoItem.qty;
                    // modelItem.productId = dtoItem.productId;
                    productList.add(modelItem);
                }
            }
            if (productAdapter != null) productAdapter.notifyDataSetChanged();

            String displayId = data.order.id != null ? data.order.id.toUpperCase(Locale.ROOT) : "---";

            binding.tvShippingFeeValue.setText(formatVnd(data.order.orderShippingFee));
            binding.tvTotalAmountValue.setText(formatVnd(data.order.orderTotalAmount));

            if (data.order.orderShippingAddress != null) {
                var a = data.order.orderShippingAddress;
                binding.tvReceiverNameValue.setText(safe(a.name));
                binding.tvReceiverPhoneValue.setText(safe(a.phone));

                String finalAddress;
                if (a.fullAddress != null && !a.fullAddress.isEmpty()) {
                    finalAddress = a.fullAddress;
                } else {

                    List<String> parts = new ArrayList<>();
                    if (!safe(a.street).isEmpty()) parts.add(safe(a.street));
                    if (!safe(a.ward).isEmpty()) parts.add(safe(a.ward));
                    if (!safe(a.province).isEmpty()) parts.add(safe(a.province));

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.size(); i++) {
                        sb.append(parts.get(i));
                        if (i < parts.size() - 1) sb.append(", ");
                    }
                    finalAddress = sb.toString();
                }
                binding.tvReceiverAddressValue.setText(finalAddress);
            } else {
                binding.tvReceiverNameValue.setText("---");
                binding.tvReceiverPhoneValue.setText("---");
                binding.tvReceiverAddressValue.setText("---");
            }

            String methodCode = data.order.orderPaymentMethod;
            String methodText = "Thanh toán điện tử";
            if (methodCode != null) {
                switch (methodCode.toLowerCase()) {
                    case "cod": methodText = "Thanh toán khi nhận hàng"; break;
                    case "zalopay": methodText = "Ví điện tử ZaloPay"; break;
                    case "wallet": methodText = "Tiền trong ví"; break;
                    case "bank": methodText = "Chuyển khoản ngân hàng"; break;
                }
            }
            binding.tvPaymentMethodValue.setText(methodText);

            binding.tvShippingMethodValue.setText("Vận chuyển tiêu chuẩn");

            binding.tvCanceledStatus.setText("Đơn hàng đã bị hủy");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi bind data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
