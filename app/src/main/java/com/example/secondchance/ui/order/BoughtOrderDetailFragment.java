package com.example.secondchance.ui.order;

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
import com.example.secondchance.databinding.FragmentBoughtOrderDetailBinding;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.ui.order.dialog.RefundConfirmDialogFragment;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.data.model.TrackingStatus;
import com.example.secondchance.ui.order.adapter.TrackingStatusAdapter;
import java.util.ArrayList;
import java.util.List;

public class BoughtOrderDetailFragment extends Fragment implements RefundConfirmDialogFragment.RefundConfirmListener {
    private static final String TAG = "BoughtDetailFrag";
    private FragmentBoughtOrderDetailBinding binding;
    private String receivedOrderId;
    private boolean receivedIsEvaluated;
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
            receivedIsEvaluated = getArguments().getBoolean("isEvaluated");
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

        updateBottomButtons();

        binding.btnReturnOrder.setOnClickListener(v -> {
            showRefundConfirmDialog();
        });
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

    private void setupProductRecyclerView() {
        productAdapter = new OrderItemAdapter(getContext(), productList);

        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);

        Log.d(TAG, "Product RecyclerView setup complete.");
    }
    private void loadDummyProductData() {
        productList.clear();
        productList.add(new OrderItem(R.drawable.nhan1, "Nhẫn Kim Cương Hữu Hạn", "Loại 1, Hãng abc thành phố Xuân Hợp", "50.000"));
        productList.add(new OrderItem(R.drawable.sample_flower, "Vòng Tay Vàng 24K", "Giỏ hoa loại 1 new 99%", "150.000"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
