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
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.data.repo.OrderRepository;
import com.example.secondchance.databinding.FragmentConfirmOrderDetailBinding;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.ui.order.dialog.ConfirmCancelDialog;
import com.example.secondchance.ui.order.dialog.CancelSuccessDialog;
import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.google.gson.Gson;

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
    private OrderRepository orderRepository;

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
        orderRepository = new OrderRepository();

        if (receivedOrderId != null) {
            //loadOrderDetails(receivedOrderId);
            loadData(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null, cannot load details.");
            Toast.makeText(getContext(), "Không thể tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }

        setupRecyclerView();
        updateBottomSection();
    }

    private void loadData(String orderId) {
        Log.d(TAG, "Fetching details for order " + orderId);

        orderRepository.getOrderDetails(orderId, new OrderRepository.RepoCallback<OrderDetailResponse.Data>() {
            @Override

            public void onSuccess(OrderDetailResponse.Data data) {
                if (!isAdded()) return;
                Gson gson = new Gson();
                Log.d(TAG, gson.toJson(data.shipment));
                Log.d(TAG, String.valueOf(data.shipment != null));

                productList.clear();
                if (data.order != null && data.order.orderItems != null) {
                    for (OrderDetailResponse.OrderItem dtoItem : data.order.orderItems) {
                        // Chuyển từ DTO (dtoItem) sang Model (modelItem)
                        com.example.secondchance.data.model.OrderItem modelItem = new com.example.secondchance.data.model.OrderItem();
                        modelItem.name = dtoItem.name;
                        modelItem.imageUrl = dtoItem.imageUrl;
                        modelItem.price = dtoItem.price;
                        modelItem.quantity = dtoItem.qty;
                        productList.add(modelItem);
                    }
                }

                if (orderItemAdapter != null) {
                    orderItemAdapter.notifyDataSetChanged();
                }

                if (data.order != null) {
                    binding.tvShippingFee.setText(formatVnd(data.order.orderShippingFee));
                    binding.tvTotalAmount.setText(formatVnd(data.order.orderTotalAmount));

                    String paymentMethod = "cod".equalsIgnoreCase(data.order.orderPaymentMethod) ? "Tiền mặt" : "Ví";
                    binding.tvPaymentMethod.setText(paymentMethod);

                    if (data.order.orderShippingAddress != null) {
                        var a = data.order.orderShippingAddress;
                        binding.tvReceiverName.setText(safe(a.name));
                        binding.tvReceiverPhone.setText(safe(a.phone));
                        String address = safe(a.street) + ", " + safe(a.ward) + ", " + safe(a.province);
                        binding.tvReceiverAddress.setText(address);
                    }
                    
                }
                if (data.order != null && data.order.orderStatus == 3) {
                    receivedOrderType = Order.OrderType.CONFIRMED_AUCTION;
                } else if (data.shipment != null){
                    Log.d(TAG, "Chuyen sang confirmed");
                    receivedOrderType = Order.OrderType.CONFIRMED_FIXED;
                } else {
                    receivedOrderType = Order.OrderType.UNCONFIRMED;
                }
                
                updateBottomSection();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;

                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
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

        orderRepository.cancelOrder(orderId, new OrderRepository.RepoCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded()) return;

                showSuccessDialog();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) return;

                Toast.makeText(getContext(), "Hủy đơn thất bại: " + message, Toast.LENGTH_SHORT).show();
            }
        });

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

    private String formatVnd(long amount) {

        return NumberFormat.getInstance(Locale.GERMAN).format(amount);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
