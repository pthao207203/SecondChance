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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.databinding.FragmentRefundOrderDetailBinding;
import com.example.secondchance.viewmodel.SharedViewModel;

public class RefundOrderDetailFragment extends Fragment {
    private static final String TAG = "RefundDetail";
    private FragmentRefundOrderDetailBinding binding;
    private SharedViewModel sharedViewModel;
    private String orderId;
    private Order.RefundStatus currentStatus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Nhận arguments
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
            currentStatus = (Order.RefundStatus) getArguments().getSerializable("refundStatus");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRefundOrderDetailBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel.updateTitle("Chi tiết Hoàn trả");

        // Kiểm tra dữ liệu
        if (currentStatus == null || orderId == null) {
            Log.e(TAG, "Lỗi: Thiếu orderId hoặc refundStatus!");
            Toast.makeText(getContext(), "Lỗi tải dữ liệu chi tiết.", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        Log.d(TAG, "Tải chi tiết cho Order ID: " + orderId + " - Trạng thái: " + currentStatus.name());

        // TODO: Tải dữ liệu chi tiết (tên SP, ảnh, lý do...) từ API/ViewModel dùng orderId

        // Quyết định layout nào sẽ được hiển thị (ẩn/hiện các nhóm layout (LinearLayout) dựa trên trạng thái)
        setupUIForStatus(currentStatus);
    }

    private void setupUIForStatus(Order.RefundStatus status) {

        binding.layoutStatusNotConfirmed.setVisibility(View.GONE);
        binding.layoutStatusConfirmed.setVisibility(View.GONE);
        binding.layoutStatusRejected.setVisibility(View.GONE);
        binding.layoutStatusSuccessful.setVisibility(View.GONE);

        // HIỆN layout tương ứng
        switch (status) {
            case NOT_CONFIRMED:
                binding.layoutStatusNotConfirmed.setVisibility(View.VISIBLE);
                binding.layoutCommonReason.setVisibility(View.GONE);
                break;

            case CONFIRMED:
                binding.layoutStatusConfirmed.setVisibility(View.VISIBLE);
                break;

            case REJECTED:
                binding.layoutStatusRejected.setVisibility(View.VISIBLE);
                break;

            case SUCCESSFUL:
                binding.layoutStatusSuccessful.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}