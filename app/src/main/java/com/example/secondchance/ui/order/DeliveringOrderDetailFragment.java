package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.databinding.FragmentDeliveringOrderDetailBinding;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.TrackingStatus;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.ui.order.adapter.TrackingStatusAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class DeliveringOrderDetailFragment extends Fragment {
    private static final String TAG = "DeliveringDetailFrag";
    private FragmentDeliveringOrderDetailBinding binding;
    private String receivedOrderId;
    private List<TrackingStatus> trackingList = new ArrayList<>();
    private TrackingStatusAdapter trackingAdapter;
    private Order.DeliveryOverallStatus receivedDeliveryStatus;
    private OrderItemAdapter productAdapter;
    private List<OrderItem> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeliveringOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            try {
                receivedDeliveryStatus = (Order.DeliveryOverallStatus) getArguments().getSerializable("deliveryStatus");
            } catch (Exception e) {
                Log.e(TAG, "Error getting deliveryStatus from arguments", e);
                receivedDeliveryStatus = Order.DeliveryOverallStatus.PACKAGED;
            }
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // RecyclerView cho trạng thái vận chuyển
        setupTrackingRecyclerView();

        // RecyclerView cho danh sách sản phẩm
        setupProductRecyclerView();

        // Tải dữ liệu chi tiết
        if (receivedOrderId != null) {
            loadDeliveringOrderDetails(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null.");
            Toast.makeText(getContext(), "Lỗi tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }

        // Xử lý nút "ĐÃ NHẬN HÀNG"
        binding.btnReceiveOrder.setOnClickListener(v -> {
            Log.d(TAG, "Receive Order button clicked for order: " + receivedOrderId);
            Toast.makeText(getContext(), "Xử lý xác nhận đã nhận hàng...", Toast.LENGTH_SHORT).show();
        });
    }

    //  RV SẢN PHẨM
    private void setupProductRecyclerView() {
        productAdapter = new OrderItemAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);

        Log.d(TAG, "Product RecyclerView setup complete.");
    }


    // tải dữ liệu chi tiết
    private void loadDeliveringOrderDetails(String orderId) {
        Log.d(TAG, "Placeholder: Load delivering order details for " + orderId);

        // Cập nhật Stepper theo trạng thái nhận được
        if (receivedDeliveryStatus != null) {
            updateStepper(receivedDeliveryStatus);
        } else {
            updateStepper(Order.DeliveryOverallStatus.PACKAGED);
        }

        // CẬP NHẬT DANH SÁCH SẢN PHẨM
        loadDummyProductData();
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product list updated for RecyclerView");
        }

        // Cập nhật danh sách tracking
        loadTrackingDataBasedOnStatus(receivedDeliveryStatus);
        if (trackingAdapter != null) {
            trackingAdapter.notifyDataSetChanged();
            Log.d(TAG,"Tracking list updated");
        }
        // TODO: Cập nhật các TextView khác (Tổng cộng, Người nhận, SDT, Địa chỉ, Vận chuyển...)
    }

    private void loadDummyProductData() {
        productList.clear();
        productList.add(new OrderItem(R.drawable.nhan1, "Nhẫn Kim Cương Hữu Hạn", "Loại 1, Hãng abc thành phố Xuân Hợp", "₫ 50.000"));
        productList.add(new OrderItem(R.drawable.sample_flower, "Vòng Tay Vàng 24K", "Giỏ hoa loại 1 new 99%", "₫ 150.000"));
        // TODO: logic cập nhật productList.addAll(dataAPI); ( dữ liệu thật)
    }

    // Tạo đối tượng TrackingStatus ĐỒNG NHẤT VỚI STEPPER
    private void loadTrackingDataBasedOnStatus(Order.DeliveryOverallStatus status) {
        trackingList.clear();

        // tạo danh sách theo thứ tự CŨ NHẤT -> MỚI NHẤT
        List<TrackingStatus> temp = new ArrayList<>();

        // Gói hàng (PACKAGED) - là cái cũ nhất
        if (status.ordinal() >= Order.DeliveryOverallStatus.PACKAGED.ordinal()) {
            temp.add(new TrackingStatus("10:00, 25/10/2025", "Gói hàng đã được đóng gói", false));
        }

        // Đã tới bưu cục (AT_POST_OFFICE)
        if (status.ordinal() >= Order.DeliveryOverallStatus.AT_POST_OFFICE.ordinal()) {
            temp.add(new TrackingStatus("15:30, 25/10/2025", "Đã đến bưu cục ABC", false));
        }

        // Đang giao (DELIVERING)
        if (status.ordinal() >= Order.DeliveryOverallStatus.DELIVERING.ordinal()) {
            temp.add(new TrackingStatus("08:00, 26/10/2025", "Đang trên đường giao đến bạn", false));
        }

        // Giao hàng thành công (DELIVERED)
        if (status.ordinal() >= Order.DeliveryOverallStatus.DELIVERED.ordinal()) {
            temp.add(new TrackingStatus("XX:XX, XX/XX/2025", "Giao hàng thành công", false));
        }

        // Gán lại cho trackingList
        trackingList.addAll(temp);

        // Đặt item CUỐI CÙNG (mới nhất) là active = true
        if (!trackingList.isEmpty()) {
            trackingList.get(trackingList.size() - 1).setActive(true);
        }
    }

    private void setupTrackingRecyclerView() {
        trackingAdapter = new TrackingStatusAdapter(getContext(), trackingList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true); // ĐẢO NGƯỢC THỨ TỰ HIỂN THỊ
        layoutManager.setStackFromEnd(true); // Đẩy nội dung từ cuối lên (giúp cuộn mượt hơn)

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

        // Reset
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

        // Kích hoạt
        switch (status) {
            case DELIVERED:
                step4Icon.setBackgroundResource(activeIcon);
                step4Label.setTextColor(activeTextColor);
                step3Line.setBackgroundColor(activeColor);
                // Fall through
            case DELIVERING:
                step3Icon.setBackgroundResource(activeIcon);
                step3Label.setTextColor(activeTextColor);
                step2Line.setBackgroundColor(activeColor);
                // Fall through
            case AT_POST_OFFICE:
                step2Icon.setBackgroundResource(activeIcon);
                step2Label.setTextColor(activeTextColor);
                step1Line.setBackgroundColor(activeColor);
                // Fall through
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