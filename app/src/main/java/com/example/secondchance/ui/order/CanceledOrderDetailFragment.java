package com.example.secondchance.ui.order;

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
    private List<OrderItem> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCanceledOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId"); // Lấy orderId
            Log.d(TAG, "Received Order ID: " + receivedOrderId);
        } else {
            Log.w(TAG, "Arguments are null!");
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupProductRecyclerView();

        if (receivedOrderId != null) {
            loadCanceledOrderDetails(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null.");
            Toast.makeText(getContext(), "Lỗi tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }

        binding.btnBuyAgain.setOnClickListener(v -> {
            Log.d(TAG, "Buy Again clicked for order: " + receivedOrderId);
            Toast.makeText(getContext(), "Xử lý Mua lại...", Toast.LENGTH_SHORT).show();
            // TODO: logic gọi API hoặc navigate đến trang sản phẩm
        });

    }

    private void setupProductRecyclerView() {
        productAdapter = new OrderItemAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        Log.d(TAG, "Product RecyclerView setup complete.");
    }
    
    private void loadCanceledOrderDetails(String orderId) {
        OrderApi api = RetrofitProvider.order();
        api.getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call,
                                   @NonNull Response<OrderDetailResponse> resp) {
                if (!isAdded()) return;
                
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(getContext(), "HTTP " + resp.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                OrderDetailResponse body = resp.body();
                if (!body.success || body.data == null || body.data.order == null) {
                    Toast.makeText(getContext(), "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindOrderDetailToViews(body.data);
            }
            
            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), t.getMessage() != null ? t.getMessage() : "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void bindOrderDetailToViews(OrderDetailResponse.Data data) {
        // 1) Bind danh sách sản phẩm vào RecyclerView
        productList.clear();
        if (data.order.orderItems != null) {
            for (OrderDetailResponse.OrderItem it : data.order.orderItems) {
                // --- Mapping sang model OrderItem của app ---
                // Điều chỉnh theo constructor/tham số của bạn.
                // Ví dụ nếu OrderItem(imageUrl, title, priceStr, qtyStr):
                productList.add(new OrderItem(
                    it.productId,
                    it.name,
                    it.imageUrl,
                    it.price,
                    it.qty
                ));
            }
        }
        if (productAdapter != null) productAdapter.notifyDataSetChanged();
        
        // 2) Phí vận chuyển & Tổng tiền
        binding.tvShippingFeeValue.setText(formatVnd(data.order.orderShippingFee));
        binding.tvTotalAmountValue.setText(formatVnd(data.order.orderTotalAmount));
        
        // 3) Người nhận, SDT, Địa chỉ
        if (data.order.orderShippingAddress != null) {
            OrderDetailResponse.ShippingAddress a = data.order.orderShippingAddress;
            binding.tvReceiverNameValue.setText(safe(a.name));
            binding.tvReceiverPhoneValue.setText(safe(a.phone));
            binding.tvReceiverAddressValue.setText(joinAddress(a.street, a.ward, a.province, a.country));
        } else {
            binding.tvReceiverNameValue.setText("");
            binding.tvReceiverPhoneValue.setText("");
            binding.tvReceiverAddressValue.setText("");
        }
        
        // 4) Phương thức thanh toán
        binding.tvPaymentMethodValue.setText(mapPayment(data.order.orderPaymentMethod));
        
        // 5) Phương thức vận chuyển
        // JSON chi tiết chưa có field shipping method => placeholder
        binding.tvShippingMethodValue.setText("Vận chuyển tiêu chuẩn");
        
        // 6) Trạng thái đơn (orderStatus = 3 theo payload bạn gửi)
        binding.tvCanceledStatus.setText("Đơn đã bị hủy");
    }
    
    // --------- Helpers ----------
    private String formatVnd(long amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount);
    }
    
    private String safe(String s) {
        return s == null ? "" : s;
    }
    
    private String joinAddress(String street, String ward, String province, String country) {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isEmpty()) sb.append(street);
        if (ward != null && !ward.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(ward);
        if (province != null && !province.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(province);
        if (country != null && !country.isEmpty()) sb.append(sb.length() > 0 ? ", " : "").append(country);
        return sb.toString();
    }
    
    private String mapPayment(String method) {
        if (method == null) return "—";
        switch (method.toLowerCase()) {
            case "cod":    return "Thanh toán khi nhận (COD)";
            case "wallet": return "Ví nội bộ";
            case "bank":   return "Chuyển khoản";
            default:       return method;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }
}
