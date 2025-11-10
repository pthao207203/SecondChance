package com.example.secondchance.ui.order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.databinding.FragmentDeliveringOrderDetailBinding;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.TrackingStatus;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.ui.order.adapter.TrackingStatusAdapter;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveringOrderDetailFragment extends Fragment {
    private static final String TAG = "DeliveringDetailFrag";
    private FragmentDeliveringOrderDetailBinding binding;
    private String receivedOrderId;
    private List<TrackingStatus> trackingList = new ArrayList<>();
    private TrackingStatusAdapter trackingAdapter;
    private OrderItemAdapter productAdapter;
    private List<OrderItem> productList = new ArrayList<>();

    private OrderApi orderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDeliveringOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");

        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupTrackingRecyclerView();
        setupProductRecyclerView();

        binding.btnReceiveOrder.setOnClickListener(v -> showConfirmReceiptDialog());

        orderApi = RetrofitProvider.order();

        if (receivedOrderId != null) {
            loadOrderDetail(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null.");
            Toast.makeText(getContext(), "Lỗi tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOrderDetail(String id) {

        orderApi.getOrderDetail(id).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> res) {
                Gson gson = new Gson();
                Log.d("DeliveringDetailFrag", "Response: " + gson.toJson(res.body().data));
                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Toast.makeText(requireContext(), "Không tải được chi tiết đơn", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindOrder(res.body().data);
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {

                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOrder(OrderDetailResponse.Data data) {
        // 1) Sản phẩm
        productList.clear();
        if (data.order != null && data.order.orderItems != null) {
            for (OrderDetailResponse.OrderItem dtoItem : data.order.orderItems) {
                com.example.secondchance.data.model.OrderItem modelItem = new com.example.secondchance.data.model.OrderItem();
                modelItem.name = dtoItem.name;
                modelItem.imageUrl = dtoItem.imageUrl;
                modelItem.price = dtoItem.price;
                modelItem.quantity = dtoItem.qty;
                productList.add(modelItem);
            }
        }
        productAdapter.notifyDataSetChanged();

        if (data.order != null) {
            binding.tvOrderId.setText("#" + data.order.id.toUpperCase(Locale.ROOT));

            if (data.order.orderShippingAddress != null) {
                var a = data.order.orderShippingAddress;
                binding.tvReceiverName.setText(safe(a.name));
                binding.tvReceiverPhone.setText(safe(a.phone));
                binding.tvReceiverAddress.setText(safe(a.street) + ", " + safe(a.ward) + ", " + safe(a.province));
            }
        }

        trackingList.clear();
        if (data.shipment != null && data.shipment.events != null) {
            for (OrderDetailResponse.Event e : data.shipment.events) {
                trackingList.add(new com.example.secondchance.data.model.TrackingStatus(
                        formatVnTime(e.eventTime),
                        mapStatusTitle(e),
                        false
                ));
            }
        }

        if (!trackingList.isEmpty()) {
            trackingList.get(trackingList.size() - 1).setActive(true);
        }
        trackingAdapter.notifyDataSetChanged();

        int shipmentStatus = (data.shipment != null) ? data.shipment.currentStatus : 0;

        Order.DeliveryOverallStatus currentStatus = Order.DeliveryOverallStatus.PACKAGED;
        if (data.order.orderStatus >= 3) { // 6 = DELIVERED
            currentStatus = Order.DeliveryOverallStatus.DELIVERED;
        } else if (shipmentStatus >= 5) { // 5 = OUT_FOR_DELIVERY
            currentStatus = Order.DeliveryOverallStatus.DELIVERING;
        } else if (shipmentStatus >= 2) { // 2, 3 = AT_POST_OFFICE
            currentStatus = Order.DeliveryOverallStatus.AT_POST_OFFICE;
        }
        updateStepper(currentStatus);

        if (currentStatus == Order.DeliveryOverallStatus.DELIVERING) {
            binding.btnReceiveOrder.setVisibility(View.VISIBLE);
        } else {
            binding.btnReceiveOrder.setVisibility(View.GONE);
        }
    }

    private String mapStatusTitle(OrderDetailResponse.Event e) {
        if (e == null) return "Cập nhật";

        if (e.eventCode == null) return safe(e.description);
        switch (e.eventCode) {
            case 1: return "Đơn vị vận chuyển lấy hàng thành công";
            case 2: return "Đơn hàng đã đến bưu cục";
            case 3: return "Đơn hàng đã rời bưu cục";
            case 4: return "Đang giao hàng";
            case 5: return "Giao hàng thành công";
            case 6: return "Giao thất bại";
            case 7: return "Khởi tạo trả hàng";
            default: return "Cập nhật";
        }
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

    private void showConfirmReceiptDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_receipt, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView).setCancelable(true).create();
        Button btnCancel = dialogView.findViewById(R.id.btnConfirmCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnKeepOrder);
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            confirmDelivery(receivedOrderId);
        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
    
    private void confirmDelivery(String orderId) {
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(requireContext(), "Thiếu mã đơn hàng", Toast.LENGTH_SHORT).show();
            binding.btnReceiveOrder.setEnabled(true);
            binding.btnReceiveOrder.setText(R.string.receive_order); // hoặc "Đã nhận hàng"
            return;
        }
        
        orderApi.confirmDelivery(orderId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> res) {
                binding.btnReceiveOrder.setEnabled(true);
                binding.btnReceiveOrder.setText(R.string.receive_order);
                
                if (!res.isSuccessful() || res.body() == null || !res.body().success) {
                    String msg = "Xác nhận thất bại (" + res.code() + ")";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Thành công: cập nhật UI + hiện dialog tiếp theo
                updateStepper(Order.DeliveryOverallStatus.DELIVERED);
                binding.btnReceiveOrder.setVisibility(View.GONE);
                showAfterConfirmDialog();
            }
            
            @Override
            public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                binding.btnReceiveOrder.setEnabled(true);
                binding.btnReceiveOrder.setText(R.string.receive_order);
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    
    private void showAfterConfirmDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_after_confirm_receipt, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView).setCancelable(true).create();
        Button btnLater = dialogView.findViewById(R.id.btnConfirmCancel);
        Button btnReview = dialogView.findViewById(R.id.btnKeepOrder);
        btnLater.setOnClickListener(v -> {
            dialog.dismiss();
            SharedViewModel vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            int targetTabIndex = 2;
            try {
                vm.requestTabChange(targetTabIndex);
            } catch (Exception e) {
                Log.w(TAG, "Failed to request tab change: " + e.getMessage());
            }
            try {
                NavHostFragment.findNavController(this).popBackStack();
            } catch (Exception e) {
                Log.w(TAG, "Failed to pop back stack: " + e.getMessage());
            }
        });
        btnReview.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Chuyển đến màn hình đánh giá...", Toast.LENGTH_SHORT).show();

        });
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void setupProductRecyclerView() {
        productAdapter = new OrderItemAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        Log.d(TAG, "Product RecyclerView setup complete.");
    }

    private void setupTrackingRecyclerView() {
        trackingAdapter = new TrackingStatusAdapter(trackingList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        binding.rvTrackingStatus.setLayoutManager(layoutManager);
        binding.rvTrackingStatus.setAdapter(trackingAdapter);
        binding.rvTrackingStatus.setNestedScrollingEnabled(false);
        Log.d(TAG, "Tracking RecyclerView setup complete (Reverse Layout).");
    }

    private void updateStepper(Order.DeliveryOverallStatus status) {
        if (binding == null || status == null || getContext() == null) return;
        Log.d(TAG, "Updating stepper for status: " + status);

        ImageView step1Icon = binding.stepperLayout.findViewById(R.id.step1_icon);
        ImageView step2Icon = binding.stepperLayout.findViewById(R.id.step2_icon);
        ImageView step3Icon = binding.stepperLayout.findViewById(R.id.step3_icon);
        ImageView step4Icon = binding.stepperLayout.findViewById(R.id.step4_icon);
        View step1Line = binding.stepperLayout.findViewById(R.id.step1_line);
        View step2Line = binding.stepperLayout.findViewById(R.id.step2_line);
        View step3Line = binding.stepperLayout.findViewById(R.id.step3_line);
        TextView step1Label = binding.stepperLayout.findViewById(R.id.step1_label);
        TextView step2Label = binding.stepperLayout.findViewById(R.id.step2_label);
        TextView step3Label = binding.stepperLayout.findViewById(R.id.step3_label);
        TextView step4Label = binding.stepperLayout.findViewById(R.id.step4_label);
        int activeColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.highLight4);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int activeIcon = R.drawable.ic_active;
        int inactiveIcon = R.drawable.ic_inactive;
        step1Icon.setBackgroundResource(inactiveIcon);
        step2Icon.setBackgroundResource(inactiveIcon);
        step3Icon.setBackgroundResource(inactiveIcon);
        step4Icon.setBackgroundResource(inactiveIcon);
        step1Line.setBackgroundColor(inactiveColor);
        step2Line.setBackgroundColor(inactiveColor);
        step3Line.setBackgroundColor(inactiveColor);
        step1Label.setTextColor(inactiveTextColor);
        step2Label.setTextColor(inactiveTextColor);
        step3Label.setTextColor(inactiveTextColor);
        step4Label.setTextColor(inactiveTextColor);
        switch (status) {
            case DELIVERED:
                step4Icon.setBackgroundResource(activeIcon);
                step4Label.setTextColor(activeTextColor);
                step3Line.setBackgroundColor(activeColor);
            case DELIVERING:
                step3Icon.setBackgroundResource(activeIcon);
                step3Label.setTextColor(activeTextColor);
                step2Line.setBackgroundColor(activeColor);
            case AT_POST_OFFICE:
                step2Icon.setBackgroundResource(activeIcon);
                step2Label.setTextColor(activeTextColor);
                step1Line.setBackgroundColor(activeColor);
            case PACKAGED:
                step1Icon.setBackgroundResource(activeIcon);
                step1Label.setTextColor(activeTextColor);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }
}