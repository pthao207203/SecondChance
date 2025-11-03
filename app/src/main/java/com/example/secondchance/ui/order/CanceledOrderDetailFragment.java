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
import com.example.secondchance.databinding.FragmentCanceledOrderDetailBinding;
import com.example.secondchance.R;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import java.util.ArrayList;
import java.util.List;

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

        // Cài đặt RecyclerView cho danh sách sản phẩm
        setupProductRecyclerView();

        // Tải dữ liệu chi tiết
        if (receivedOrderId != null) {
            loadCanceledOrderDetails(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null.");
            Toast.makeText(getContext(), "Lỗi tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }

        // Xử lý nút "MUA LẠI"
        binding.btnBuyAgain.setOnClickListener(v -> {
            Log.d(TAG, "Buy Again clicked for order: " + receivedOrderId);
            Toast.makeText(getContext(), "Xử lý Mua lại...", Toast.LENGTH_SHORT).show();
            // TODO: logic gọi API hoặc navigate đến trang sản phẩm
        });

    }

    // cài đặt RV sản phẩm
    private void setupProductRecyclerView() {
        productAdapter = new OrderItemAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        Log.d(TAG, "Product RecyclerView setup complete.");
    }

    // tải dữ liệu chi tiết
    private void loadCanceledOrderDetails(String orderId) {
        Log.d(TAG, "Placeholder: Load canceled order details for " + orderId);
        // TODO: Gọi API/Database lấy thông tin chi tiết đơn hàng ĐÃ HỦY

        loadDummyProductData();
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product list updated");
        }
        // TODO: Cập nhật các TextView khác (Tổng tiền, Người nhận...)

    }

    // load sản phẩm giả
    private void loadDummyProductData() {
        productList.clear();
        productList.add(new OrderItem(R.drawable.nhan1, "Sản phẩm đã hủy 1", "Mô tả...", "10.000"));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }
}