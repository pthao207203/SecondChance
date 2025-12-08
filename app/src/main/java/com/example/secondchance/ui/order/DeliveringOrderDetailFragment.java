package com.example.secondchance.ui.order;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.model.TrackingStatus;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentDeliveringOrderDetailBinding;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.ui.order.adapter.TrackingStatusAdapter;
import com.example.secondchance.viewmodel.SharedViewModel;

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

    // Data Lists
    private final List<TrackingStatus> trackingList = new ArrayList<>();
    private final List<OrderItem> productList = new ArrayList<>();

    // Adapters
    private TrackingStatusAdapter trackingAdapter;
    private OrderItemAdapter productAdapter;
    private OrderApi orderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeliveringOrderDetailBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo Retrofit
        orderApi = RetrofitProvider.order();

        // Setup UI
        setupTrackingRecyclerView();
        setupProductRecyclerView();

        // Sự kiện nút bấm
        binding.btnReceiveOrder.setOnClickListener(v -> showConfirmReceiptDialog());

        // Load dữ liệu
        if (receivedOrderId != null && !receivedOrderId.isEmpty()) {
            loadOrderDetail(receivedOrderId);
        } else {
            Log.e(TAG, "Lỗi: Order ID bị null.");
            Toast.makeText(getContext(), "Không tìm thấy mã đơn hàng.", Toast.LENGTH_SHORT).show();
            binding.btnReceiveOrder.setVisibility(View.GONE);
        }
    }

    private void loadOrderDetail(String id) {
        orderApi.getOrderDetail(id).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> res) {
                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Toast.makeText(requireContext(), "Không tải được thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindOrder(res.body().data);
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void bindOrder(OrderDetailResponse.Data data) {
        try {
            if (data.order == null) return;

            // 1. Bind danh sách sản phẩm
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
            if (productAdapter != null) productAdapter.notifyDataSetChanged();

            // thông tin chung (Mã đơn)
            String displayId = data.order.id != null ? data.order.id.toUpperCase(Locale.ROOT) : "---";
            binding.tvOrderId.setText("#" + displayId);

            // thông tin Người Nhận & Địa Chỉ
            if (data.order.orderShippingAddress != null) {
                var a = data.order.orderShippingAddress;
                binding.tvReceiverName.setText(safe(a.name));
                binding.tvReceiverPhone.setText(safe(a.phone));

                String finalAddress;

                // fullAddress
                if (a.fullAddress != null && !a.fullAddress.isEmpty()) {
                    finalAddress = a.fullAddress;
                } else {
                    // Ghép chuỗi từ các thành phần
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

                binding.tvReceiverAddress.setText(finalAddress);

            }

            // lịch sử vận chuyển (Tracking)
            trackingList.clear();
            if (data.shipment != null && data.shipment.events != null) {
                for (OrderDetailResponse.Event e : data.shipment.events) {
                    trackingList.add(new TrackingStatus(
                            formatVnTime(e.eventTime),
                            mapStatusTitle(e),
                            false
                    ));
                }
            }

            if (!trackingList.isEmpty()) {
                trackingList.get(trackingList.size() - 1).setActive(true);
            }
            if (trackingAdapter != null) trackingAdapter.notifyDataSetChanged();

            // Cập nhật Stepper và Nút bấm
            int shipmentStatus = (data.shipment != null) ? data.shipment.currentStatus : 0;
            int orderStatus = data.order.orderStatus;

            Order.DeliveryOverallStatus currentStatus = Order.DeliveryOverallStatus.PACKAGED;

            if (orderStatus >= 3) {
                currentStatus = Order.DeliveryOverallStatus.DELIVERED;
            } else if (shipmentStatus >= 4) {
                currentStatus = Order.DeliveryOverallStatus.DELIVERING;
            } else if (shipmentStatus >= 2) {
                currentStatus = Order.DeliveryOverallStatus.AT_POST_OFFICE;
            }

            updateStepper(currentStatus);

            if (currentStatus == Order.DeliveryOverallStatus.DELIVERING) {
                binding.btnReceiveOrder.setVisibility(View.VISIBLE);
                binding.btnReceiveOrder.setEnabled(true);
            } else {
                binding.btnReceiveOrder.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error binding data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStepper(Order.DeliveryOverallStatus status) {
        if (binding == null || status == null || getContext() == null) return;

        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.highLight4);
        int activeColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int activeIcon = R.drawable.ic_active;
        int inactiveIcon = R.drawable.ic_inactive;

        // Reset
        binding.step1Icon.setBackgroundResource(inactiveIcon);
        binding.step2Icon.setBackgroundResource(inactiveIcon);
        binding.step3Icon.setBackgroundResource(inactiveIcon);
        binding.step4Icon.setBackgroundResource(inactiveIcon);
        binding.step1Line.setBackgroundColor(inactiveColor);
        binding.step2Line.setBackgroundColor(inactiveColor);
        binding.step3Line.setBackgroundColor(inactiveColor);
        binding.step1Label.setTextColor(inactiveTextColor);
        binding.step2Label.setTextColor(inactiveTextColor);
        binding.step3Label.setTextColor(inactiveTextColor);
        binding.step4Label.setTextColor(inactiveTextColor);

        switch (status) {
            case DELIVERED:
                binding.step4Icon.setBackgroundResource(activeIcon);
                binding.step4Label.setTextColor(activeTextColor);
                binding.step3Line.setBackgroundColor(activeColor);
            case DELIVERING:
                binding.step3Icon.setBackgroundResource(activeIcon);
                binding.step3Label.setTextColor(activeTextColor);
                binding.step2Line.setBackgroundColor(activeColor);
            case AT_POST_OFFICE:
                binding.step2Icon.setBackgroundResource(activeIcon);
                binding.step2Label.setTextColor(activeTextColor);
                binding.step1Line.setBackgroundColor(activeColor);
            case PACKAGED:
                binding.step1Icon.setBackgroundResource(activeIcon);
                binding.step1Label.setTextColor(activeTextColor);
                break;
        }
    }

    private void showConfirmReceiptDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_receipt, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView).setCancelable(true).create();

        dialogView.findViewById(R.id.btnConfirmCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnKeepOrder).setOnClickListener(v -> {
            dialog.dismiss();
            confirmDelivery(receivedOrderId);
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void confirmDelivery(String orderId) {
        binding.btnReceiveOrder.setEnabled(false);
        binding.btnReceiveOrder.setText("Đang xử lý...");

        orderApi.confirmDelivery(orderId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> res) {
                if (binding != null) {
                    binding.btnReceiveOrder.setEnabled(true);
                    binding.btnReceiveOrder.setText("ĐÃ NHẬN HÀNG");
                }

                if (!res.isSuccessful() || res.body() == null || !res.body().success) {
                    Toast.makeText(requireContext(), "Xác nhận thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(requireContext(), "Đã xác nhận nhận hàng!", Toast.LENGTH_SHORT).show();
                updateStepper(Order.DeliveryOverallStatus.DELIVERED);
                if (binding != null) binding.btnReceiveOrder.setVisibility(View.GONE);

                showAfterConfirmDialog();
            }

            @Override
            public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                if (binding != null) {
                    binding.btnReceiveOrder.setEnabled(true);
                    binding.btnReceiveOrder.setText("ĐÃ NHẬN HÀNG");
                }
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAfterConfirmDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_after_confirm_receipt, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView).setCancelable(true).create();

        dialogView.findViewById(R.id.btnConfirmCancel).setOnClickListener(v -> {
            dialog.dismiss();
            navigateBackOrSwitchTab();
        });

        dialogView.findViewById(R.id.btnKeepOrder).setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Chuyển đến đánh giá...", Toast.LENGTH_SHORT).show();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    private void navigateBackOrSwitchTab() {
        try {
            SharedViewModel vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            vm.requestTabChange(2);
        } catch (Exception e) {
            Log.w(TAG, "ViewModel error: " + e.getMessage());
        }
        try {
            NavHostFragment.findNavController(this).popBackStack();
        } catch (Exception e) {
            Log.w(TAG, "Nav error: " + e.getMessage());
        }
    }

    private void setupProductRecyclerView() {
        productAdapter = new OrderItemAdapter(requireContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void setupTrackingRecyclerView() {
        trackingAdapter = new TrackingStatusAdapter(trackingList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        binding.rvTrackingStatus.setLayoutManager(layoutManager);
        binding.rvTrackingStatus.setAdapter(trackingAdapter);
        binding.rvTrackingStatus.setNestedScrollingEnabled(false);
    }

    private String mapStatusTitle(OrderDetailResponse.Event e) {
        if (e == null) return "Cập nhật";
        if (e.eventCode == null) return safe(e.description);
        switch (e.eventCode) {
            case 1: return "ĐVVC đã lấy hàng";
            case 2: return "Đã đến bưu cục";
            case 3: return "Đã rời bưu cục";
            case 4: return "Đang giao hàng";
            case 5: return "Giao thành công";
            case 6: return "Giao thất bại";
            case 7: return "Đang hoàn hàng";
            default: return safe(e.description).isEmpty() ? "Đang cập nhật" : e.description;
        }
    }

    private String formatVnTime(String iso) {
        if (iso == null) return "";
        try {
            ZonedDateTime z = ZonedDateTime.parse(iso).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
            return z.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        } catch (Exception e) { return iso; }
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
