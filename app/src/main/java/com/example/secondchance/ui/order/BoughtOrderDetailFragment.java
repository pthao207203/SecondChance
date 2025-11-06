package com.example.secondchance.ui.order;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.navigation.Navigation;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.data.remote.MeApi;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentBoughtOrderDetailBinding;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.ui.order.dialog.RefundConfirmDialogFragment;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.data.model.TrackingStatus;
import com.example.secondchance.ui.order.adapter.TrackingStatusAdapter;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoughtOrderDetailFragment extends Fragment implements RefundConfirmDialogFragment.RefundConfirmListener {
    private static final String TAG = "BoughtDetailFrag";
    private FragmentBoughtOrderDetailBinding binding;
    private String receivedOrderId;
    private boolean receivedIsEvaluated;
    private OrderItemAdapter productAdapter;
    private List<OrderItem> productList = new ArrayList<>();
    private TrackingStatusAdapter trackingAdapter;
    private List<TrackingStatus> trackingList = new ArrayList<>();
    private OrderApi orderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBoughtOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            receivedIsEvaluated = getArguments().getBoolean("isEvaluated");
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Recycler: sản phẩm
        productAdapter = new OrderItemAdapter(requireContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        
        // Recycler: timeline vận chuyển
//        trackingAdapter = new TrackingStatusAdapter(trackingList);
//        binding.rvTracking.setLayoutManager(new LinearLayoutManager(requireContext()));
//        binding.rvTracking.setAdapter(trackingAdapter);
        
        updateBottomButtons();
        
        binding.btnReturnOrder.setOnClickListener(v -> showRefundConfirmDialog());
        
        // Gọi API
        if (receivedOrderId == null || receivedOrderId.isEmpty()) {
            Toast.makeText(requireContext(), "Thiếu orderId", Toast.LENGTH_LONG).show();
            return;
        }
        orderApi = RetrofitProvider.order();
        loadOrderDetail(receivedOrderId);
    }
    private void loadOrderDetail(String id) {
        showLoading(true);
        orderApi.getOrderDetail(id).enqueue(new Callback<OrderDetailResponse>() {
            @Override public void onResponse(@NonNull Call<OrderDetailResponse> call,
                                             @NonNull Response<OrderDetailResponse> res) {
                showLoading(false);
                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Toast.makeText(requireContext(), "Không tải được chi tiết đơn", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindOrder(res.body().data);
            }
            
            @Override public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void bindOrder(OrderDetailResponse.Data data) {
        // 1) Sản phẩm
        productList.clear();
        if (data.order != null && data.order.orderItems != null) {
            for (OrderDetailResponse.OrderItem it : data.order.orderItems) {
                productList.add(new com.example.secondchance.data.model.OrderItem(
                  it.imageUrl,                                // << URL ảnh từ backend
                  safe(it.name),
                  "SL: " + it.qty,
                  formatVnd(it.price)
                ));
            }
        }
        productAdapter.notifyDataSetChanged();
        
        
        // 2) Tổng tiền + địa chỉ + trạng thái
        if (data.order != null) {
            binding.tvOrderId.setText("#" + data.order.id.toUpperCase(Locale.ROOT));
            binding.tvShippingFee.setText(formatVnd(data.order.orderShippingFee));
            binding.tvTotalAmount.setText(formatVnd(data.order.orderTotalAmount));
            binding.tvPaymentMethod.setText(safe(data.order.orderPaymentMethod).equals("cod") ? "Tiền mặt" : "Ví của tôi");
            
            if (data.order.orderShippingAddress != null) {
                var a = data.order.orderShippingAddress;
                binding.tvReceiverName.setText(safe(a.name));
                binding.tvReceiverPhone.setText(safe(a.phone));
                binding.tvReceiverAddress.setText(safe(a.street) + ", " + safe(a.ward) + ", " + safe(a.province));
            }
            
//            binding.tvCreatedAt.setText(formatVnTime(data.order.createdAt));
//            binding.tvUpdatedAt.setText(formatVnTime(data.order.updatedAt));
        }
        
        // 3) Timeline vận chuyển
//        trackingList.clear();
//        if (data.shipment != null && data.shipment.events != null) {
//            for (OrderDetailResponse.Event e : data.shipment.events) {
//                trackingList.add(new com.example.secondchance.data.model.TrackingStatus(
//                  mapStatusTitle(e),                 // title
//                  formatVnTime(e.eventTime),         // time
//                  safe(e.description)                // subtitle / description
//                ));
//            }
//            // (tùy UI) sort theo thời gian tăng hoặc giảm
//            Collections.sort(trackingList, Comparator.comparing(ts -> ts.getTime()));
//        }
//        trackingAdapter.notifyDataSetChanged();
    }
    
    private String mapStatusTitle(OrderDetailResponse.Event e) {
        if (e == null) return "Cập nhật";
        if (e.eventCode == null) return upperFirst(safe(e.description));
        switch (e.eventCode) {
            case 1: return "Nhận hàng từ shop";
            case 2: return "Đến kho";
            case 3: return "Rời kho";
            case 4: return "Phát/Hoàn trả";
            case 5: return "Đã giao";
            default: return "Cập nhật";
        }
    }
    private void showRefundConfirmDialog() {
        RefundConfirmDialogFragment dialog = new RefundConfirmDialogFragment();
        dialog.setListener(this);
        dialog.show(getParentFragmentManager(), "RefundConfirmDialogTag");
    }

    @Override
    public void onRefundConfirmed() {
        Toast.makeText(requireContext(), "Đã xác nhận. Chuyển sang màn hình TẠO YÊU CẦU HOÀN TRẢ...", Toast.LENGTH_LONG).show();

        Bundle bundle = new Bundle();

        if (receivedOrderId != null) {
            bundle.putString("orderId", receivedOrderId);
        }

        try {

            Navigation.findNavController(requireView()).navigate(
                    R.id.action_boughtOrderDetailFragment_to_createOrderReturnRequestFragment,
                    bundle
            );
        } catch (Exception e) {
            Log.e(TAG, "Lỗi điều hướng đến CreateOrderReturnRequestFragment", e);
            Toast.makeText(requireContext(), "Lỗi: Không thể chuyển màn hình. Thiếu action điều hướng.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRefundCancelled() {
        Toast.makeText(requireContext(), "Yêu cầu hoàn trả đã bị hủy.", Toast.LENGTH_SHORT).show();
    }


    private void updateBottomButtons() {
        if (receivedIsEvaluated) {
            binding.btnRateShop.setVisibility(View.GONE);
            Log.d(TAG, "Order already evaluated. Hiding Rate button.");
        } else {
            binding.btnRateShop.setVisibility(View.VISIBLE);
            Log.d(TAG, "Order NOT evaluated. Showing Rate button.");

            binding.btnRateShop.setOnClickListener(v -> {
                Log.d(TAG, "Rate Shop clicked for order: " + receivedOrderId);
                Toast.makeText(getContext(), "Mở màn hình Đánh giá...", Toast.LENGTH_SHORT).show();
            });
        }
    }
    private void showLoading(boolean show) {
//        if (binding.progress != null) binding.progress.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.getRoot().setAlpha(show ? 0.6f : 1f);
    }
    
    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi","VN")).format(amount);
    }
    private String formatVnTime(String iso) {
        try {
            ZonedDateTime z = ZonedDateTime.parse(iso).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
            return z.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) { return iso; }
    }
    private String safe(String s) { return s == null ? "" : s; }
    private String upperFirst(String s) { return s.isEmpty()? s : s.substring(0,1).toUpperCase()+s.substring(1); }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
