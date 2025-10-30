package com.example.secondchance.ui.order;

import com.example.secondchance.R;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import java.util.ArrayList;
import java.util.List;
import com.example.secondchance.databinding.FragmentConfirmOrderDetailBinding;
import com.example.secondchance.data.model.Order;

public class ConfirmOrderDetailFragment extends Fragment {
    private static final String TAG = "ConfirmOrderDetailFrag";
    private FragmentConfirmOrderDetailBinding binding;
    private String receivedOrderId;
    private Order.OrderType receivedOrderType;
    private OrderItemAdapter orderItemAdapter;
    private List<OrderItem> productList = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmOrderDetailBinding.inflate(inflater, container, false);
        // Lấy arguments
        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            try {
                receivedOrderType = (Order.OrderType) getArguments().getSerializable("orderType");
                Log.d(TAG, "Received Order ID: " + receivedOrderId + ", Type: " + receivedOrderType);
            } catch (Exception e) {
                Log.e(TAG, "Error getting orderType from arguments", e);
                receivedOrderType = null;
            }
        } else {
            Log.w(TAG, "Arguments are null!");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (receivedOrderId != null) {
            loadOrderDetails(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null, cannot load details.");
            Toast.makeText(getContext(), "Không thể tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }

        setupRecyclerView();
        updateBottomSection();
    }

    // tải dữ liệu chi tiết
    private void loadOrderDetails(String orderId) {
        // TODO: Viết code gọi API/Database để lấy danh sách SẢN PHẨM của đơn hàng 'orderId'
        // DỮ LIỆU GIẢ
        Log.d(TAG, "Loading dummy product list for order " + orderId);
        productList.clear(); // Xóa dữ liệu cũ
        productList.add(new OrderItem(R.drawable.sample_flower, "Giỏ gỗ cắm hoa", "Mẫu A, giá cố định...", "₫ 50.000"));
        productList.add(new OrderItem(R.drawable.nhan1, "Nhẫn Hướng Dương", "Đấu giá, New 99%...", "₫ 150.000"));

        // Cập nhật Adapter (nếu đã được tạo)
        if (orderItemAdapter != null) {
            orderItemAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product list updated for RecyclerView");
        }
    }

    private void setupRecyclerView() {
        orderItemAdapter = new OrderItemAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(orderItemAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);

        Log.d(TAG, "RecyclerView setup complete with OrderItemAdapter.");
    }

    // xử lý ẩn/hiện phần cuối trang
    private void updateBottomSection() {
        binding.btnCancelOrderDetail.setVisibility(View.GONE);
        binding.cvInfoConfirmedFixed.setVisibility(View.GONE);
        binding.cvInfoAuction.setVisibility(View.GONE);

        if (receivedOrderType != null) {
            Log.d(TAG, "Updating bottom section for type: " + receivedOrderType);
            switch (receivedOrderType) {
                case UNCONFIRMED:
                    binding.btnCancelOrderDetail.setVisibility(View.VISIBLE);
                    binding.btnCancelOrderDetail.setOnClickListener(v -> {
                        Log.d(TAG, "Cancel button clicked for order: " + receivedOrderId);
                        Toast.makeText(getContext(), "Xử lý hủy đơn: " + receivedOrderId, Toast.LENGTH_SHORT).show();
                        // TODO: Thêm logic gọi API hủy đơn
                    });
                    break;
                case CONFIRMED_FIXED:
                    binding.cvInfoConfirmedFixed.setVisibility(View.VISIBLE);
                    break;
                case CONFIRMED_AUCTION:
                    binding.cvInfoAuction.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            Log.w(TAG, "OrderType is null, cannot determine bottom section.");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }
}