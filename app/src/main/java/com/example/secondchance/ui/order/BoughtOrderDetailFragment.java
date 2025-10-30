package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.databinding.FragmentBoughtOrderDetailBinding;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.data.model.TrackingStatus;
import com.example.secondchance.ui.order.adapter.TrackingStatusAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class BoughtOrderDetailFragment extends Fragment {
    private static final String TAG = "BoughtDetailFrag";
    private FragmentBoughtOrderDetailBinding binding;
    private String receivedOrderId;
    private boolean receivedIsEvaluated; // Biến lưu trạng thái đánh giá

    // Adapter cho 2 RecyclerView
    private OrderItemAdapter productAdapter;
    private List<OrderItem> productList = new ArrayList<>();
    private TrackingStatusAdapter trackingAdapter;
    private List<TrackingStatus> trackingList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBoughtOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            receivedIsEvaluated = getArguments().getBoolean("isEvaluated"); // Lấy trạng thái
            Log.d(TAG, "Received Order ID: " + receivedOrderId + ", IsEvaluated: " + receivedIsEvaluated);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupProductRecyclerView();

        loadDummyProductData();
        productAdapter.notifyDataSetChanged();

        // logic ẩn/hiện nút Đánh giá
        updateBottomButtons();

        // TODO: Gắn listener cho nút Hoàn trả
        binding.btnReturnOrder.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Xử lý Hoàn trả...", Toast.LENGTH_SHORT).show();
        });
    }

    // Ẩn/hiện nút Đánh giá
    private void updateBottomButtons() {
        if (receivedIsEvaluated) {
            // ĐÃ ĐÁNH GIÁ -> Ẩn nút "Đánh giá Shop"
            binding.btnRateShop.setVisibility(View.GONE);
            Log.d(TAG, "Order already evaluated. Hiding Rate button.");
        } else {
            // CHƯA ĐÁNH GIÁ -> Hiện nút "Đánh giá Shop"
            binding.btnRateShop.setVisibility(View.VISIBLE);
            Log.d(TAG, "Order NOT evaluated. Showing Rate button.");

            // Gắn listener cho nút
            binding.btnRateShop.setOnClickListener(v -> {
                Log.d(TAG, "Rate Shop clicked for order: " + receivedOrderId);
                Toast.makeText(getContext(), "Mở màn hình Đánh giá...", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupProductRecyclerView() {
        // tạo Adapter mới
        productAdapter = new OrderItemAdapter(getContext(), productList);

        // Cài đặt LayoutManager và gán Adapter
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);

        Log.d(TAG, "Product RecyclerView setup complete.");
    }
    private void loadDummyProductData() {
        productList.clear();
        productList.add(new OrderItem(R.drawable.nhan1, "Nhẫn Kim Cương Hữu Hạn", "Loại 1, Hãng abc thành phố Xuân Hợp", "₫ 50.000"));
        productList.add(new OrderItem(R.drawable.sample_flower, "Vòng Tay Vàng 24K", "Giỏ hoa loại 1 new 99%", "₫ 150.000"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}