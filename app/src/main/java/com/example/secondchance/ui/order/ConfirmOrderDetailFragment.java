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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.lifecycle.ViewModelProvider;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.databinding.FragmentConfirmOrderDetailBinding;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.ui.order.dialog.ConfirmCancelDialog;
import com.example.secondchance.ui.order.dialog.CancelSuccessDialog;
import java.util.ArrayList;
import java.util.List;

public class ConfirmOrderDetailFragment extends Fragment
        implements ConfirmCancelDialog.OnCancelConfirmationListener,
        CancelSuccessDialog.OnDismissListener {
    private static final String TAG = "ConfirmOrderDetailFrag";
    private FragmentConfirmOrderDetailBinding binding;
    private String receivedOrderId;
    private Order.OrderType receivedOrderType;
    private OrderItemAdapter orderItemAdapter;
    private List<OrderItem> productList = new ArrayList<>();

    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmOrderDetailBinding.inflate(inflater, container, false);

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

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        if (receivedOrderId != null) {
            loadOrderDetails(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null, cannot load details.");
            Toast.makeText(getContext(), "Không thể tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }

        setupRecyclerView();
        updateBottomSection();
    }

    private void loadOrderDetails(String orderId) {
        Log.d(TAG, "Loading dummy product list for order " + orderId);
        productList.clear();
        productList.add(new OrderItem(R.drawable.sample_flower, "Giỏ gỗ cắm hoa", "Mẫu A, giá cố định...", "50.000"));
        productList.add(new OrderItem(R.drawable.nhan1, "Nhẫn Hướng Dương", "Đấu giá, New 99%...", "150.000"));

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

    private void updateBottomSection() {
        binding.btnCancelOrderDetail.setVisibility(View.GONE);
        binding.cvInfoConfirmedFixed.setVisibility(View.GONE);
        binding.cvInfoAuction.setVisibility(View.GONE);

        if (receivedOrderType != null) {
            Log.d(TAG, "Updating bottom section for type: " + receivedOrderType);
            switch (receivedOrderType) {
                case UNCONFIRMED:
                    binding.btnCancelOrderDetail.setVisibility(View.VISIBLE);
                    // Logic hiển thị dialog HỎI
                    binding.btnCancelOrderDetail.setOnClickListener(v -> {
                        ConfirmCancelDialog dialog = new ConfirmCancelDialog(receivedOrderId, this); // 'this' là Fragment
                        dialog.show(getParentFragmentManager(), ConfirmCancelDialog.TAG);
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
    public void onCancelConfirmed(String orderId) {
        Log.d(TAG, "Đã xác nhận hủy đơn: " + orderId);
        // TODO: Gọi ViewModel để thực hiện API hủy đơn
        showSuccessDialog();
    }

    private void showSuccessDialog() {
        CancelSuccessDialog successDialog = new CancelSuccessDialog(this);
        successDialog.show(getParentFragmentManager(), CancelSuccessDialog.TAG);
    }

    @Override
    public void onSuccessfulDismiss() {
        Log.d(TAG, "Đã đóng dialog thành công. Quay lại và chuyển tab.");
        sharedViewModel.refreshOrderLists();
        sharedViewModel.requestTabChange(3);
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }
}
