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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentBoughtOrderDetailBinding;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.ui.order.dialog.RefundConfirmDialogFragment;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoughtOrderDetailFragment extends Fragment implements RefundConfirmDialogFragment.RefundConfirmListener {

    private static final String TAG = "DEBUG_ORDER";

    private FragmentBoughtOrderDetailBinding binding;
    private String receivedOrderId;
    private boolean receivedIsEvaluated;

    private OrderItemAdapter productAdapter;
    private final List<OrderItem> productList = new ArrayList<>();
    private OrderApi orderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBoughtOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            receivedIsEvaluated = getArguments().getBoolean("isEvaluated");
            Log.d(TAG, "1. onCreateView: Nhận được OrderID = " + receivedOrderId);
        } else {
            Log.e(TAG, "1. onCreateView: Không có arguments!");
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "2. Bắt đầu onViewCreated");

        try {
            orderApi = RetrofitProvider.order();
            Log.d(TAG, "3. Khởi tạo Retrofit API thành công");
        } catch (Exception e) {
            Log.e(TAG, "3. LỖI khởi tạo API: " + e.getMessage());
        }

        Log.d(TAG, "4. Chuẩn bị setup RecyclerView");
        setupProductRecyclerView();
        Log.d(TAG, "5. Setup RecyclerView hoàn tất");

        updateBottomButtons();

        binding.btnReturnOrder.setOnClickListener(v -> showRefundConfirmDialog());

        if (receivedOrderId == null || receivedOrderId.isEmpty()) {
            Toast.makeText(requireContext(), "Lỗi: Không tìm thấy mã đơn hàng", Toast.LENGTH_LONG).show();
            Log.e(TAG, "LỖI: OrderID bị null hoặc rỗng -> Dừng xử lý");
            return;
        }

        Log.d(TAG, "6. Gọi hàm loadOrderDetail với ID: " + receivedOrderId);
        loadOrderDetail(receivedOrderId);
    }

    private void loadOrderDetail(String id) {
        Log.d(TAG, "7. Đang thực thi API call (enqueue)...");

        orderApi.getOrderDetail(id).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> res) {
                Log.d(TAG, "8. API đã phản hồi! Code: " + res.code());

                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Log.e(TAG, "LỖI: Response không thành công hoặc body null");
                    Toast.makeText(requireContext(), "Không thể tải thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "9. Dữ liệu hợp lệ. Bắt đầu bindOrderData...");
                bindOrderData(res.body().data);
                Log.d(TAG, "10. Bind dữ liệu hoàn tất! (Giao diện đã cập nhật)");
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "LỖI MẠNG/API onFailure: " + t.getMessage());
                t.printStackTrace();
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void bindOrderData(OrderDetailResponse.Data data) {
        try {
            if (data.order == null) {
                Log.e(TAG, "Lỗi: data.order bị null");
                return;
            }


            productList.clear();
            if (data.order.orderItems != null) {
                for (OrderDetailResponse.OrderItem dtoItem : data.order.orderItems) {
                    OrderItem modelItem = new OrderItem();
                    modelItem.name = dtoItem.name;
                    modelItem.imageUrl = dtoItem.imageUrl;
                    modelItem.price = dtoItem.price;
                    modelItem.quantity = dtoItem.qty;
                    productList.add(modelItem);
                }
            }

            if (productAdapter != null) {
                productAdapter.notifyDataSetChanged();
            }

            String orderId = data.order.id != null ? data.order.id : "---";
            binding.tvOrderId.setText("#" + orderId.toUpperCase(Locale.ROOT));

            long shipFee = data.order.orderShippingFee;
            long totalAmt = data.order.orderTotalAmount;
            binding.tvShippingFee.setText(formatVnd(shipFee));
            binding.tvTotalAmount.setText(formatVnd(totalAmt));

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
            binding.tvPaymentMethod.setText(methodText);

            if (data.order.orderShippingAddress != null) {
                var addressData = data.order.orderShippingAddress;
                binding.tvReceiverName.setText(safeString(addressData.name));
                binding.tvReceiverPhone.setText(safeString(addressData.phone));

                String fullAddress = buildSafeAddress(
                        safeString(addressData.street),
                        safeString(addressData.ward),
                        safeString(addressData.province)
                );
                binding.tvReceiverAddress.setText(fullAddress);
            }

            Log.d(TAG, "10. Bind dữ liệu hoàn tất! (Giao diện đã cập nhật)");

        } catch (Exception e) {

            Log.e(TAG, "CRASH ERROR trong bindOrderData: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi hiển thị dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupProductRecyclerView() {
        Log.d(TAG, "   -> Bên trong setupProductRecyclerView");
        productAdapter = new OrderItemAdapter(requireContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        Log.d(TAG, "   -> Đã setAdapter xong");
    }

    private void updateBottomButtons() {
        if (receivedIsEvaluated) {
            binding.btnRateShop.setVisibility(View.GONE);
        } else {
            binding.btnRateShop.setVisibility(View.VISIBLE);
            binding.btnRateShop.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Chức năng Đánh giá đang phát triển", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showRefundConfirmDialog() {
        RefundConfirmDialogFragment dialog = new RefundConfirmDialogFragment();
        dialog.setListener(this);
        dialog.show(getParentFragmentManager(), "RefundConfirmDialogTag");
    }

    @Override
    public void onRefundConfirmed() {
        Bundle bundle = new Bundle();
        if (receivedOrderId != null) bundle.putString("orderId", receivedOrderId);

        try {
            Navigation.findNavController(requireView()).navigate(
                    R.id.action_boughtOrderDetailFragment_to_createOrderReturnRequestFragment,
                    bundle
            );
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi điều hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Lỗi điều hướng: " + e.getMessage());
        }
    }

    @Override
    public void onRefundCancelled() { }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
    }

    private String buildSafeAddress(String street, String ward, String province) {
        StringBuilder sb = new StringBuilder();
        if (!street.isEmpty()) sb.append(street);
        if (!ward.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ward);
        }
        if (!province.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(province);
        }
        return sb.toString();
    }

    private String safeString(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
