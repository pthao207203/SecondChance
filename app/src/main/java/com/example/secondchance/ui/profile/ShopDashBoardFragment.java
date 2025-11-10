package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.secondchance.databinding.FragmentShopDashboardBinding;

public class ShopDashBoardFragment extends Fragment {

    private FragmentShopDashboardBinding binding;

    public ShopDashBoardFragment() {
        // Bắt buộc có constructor rỗng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShopDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        loadDashboardData();
    }

    /**
     * Cài đặt UI ban đầu (chẳng hạn set text tiêu đề)
     */
    private void setupUI() {
        binding.tvNameChart.setText("Biểu đồ số sản phẩm đã bán theo tháng");
    }

    /**
     * Giả lập tải dữ liệu (sau có thể thay bằng API hoặc ViewModel)
     */
    private void loadDashboardData() {
        binding.tvTotalProducts.setText("256");
        binding.tvTotalpercent.setText("+5%");
        binding.tvSoldProducts.setText("26");
        binding.tvSodPer.setText("-10%");
        binding.tvRemaningProducts.setText("255");
        binding.tvRemainingpercent.setText("+5%");
        binding.tvRefundProducts.setText("0");
        binding.tvRefund.setText("0%");
        binding.tvPerMonth.setText("256");
        binding.tvPerMonthpercent.setText("+5%");
        binding.tvProfitProducts.setText("100.000đ");
        binding.tvProfitpercent.setText("+15%");

        // Ví dụ: gắn sự kiện click nếu cần
        binding.Rep.setOnClickListener(v -> {
            // Ví dụ: Toast hoặc mở màn khác
            // Toast.makeText(requireContext(), "Tỉ lệ phản hồi", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
