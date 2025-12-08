package com.example.secondchance.ui.order;

import android.annotation.SuppressLint;
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

import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.databinding.FragmentConfirmOrderDetailBinding;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.ui.order.dialog.ConfirmCancelDialog;
import com.example.secondchance.ui.order.dialog.CancelSuccessDialog;
import com.example.secondchance.dto.response.OrderDetailResponse;

import java.util.ArrayList;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmOrderDetailFragment extends Fragment
        implements ConfirmCancelDialog.OnCancelConfirmationListener,
        CancelSuccessDialog.OnDismissListener {

    private static final String TAG = "ConfirmDetailFrag";
    private FragmentConfirmOrderDetailBinding binding;
    private String receivedOrderId;
    private Order.OrderType receivedOrderType;
    private OrderItemAdapter orderItemAdapter;
    private final List<OrderItem> productList = new ArrayList<>();
    private SharedViewModel sharedViewModel;

    private OrderApi orderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            try {
                receivedOrderType = (Order.OrderType) getArguments().getSerializable("orderType");
            } catch (Exception e) {
                receivedOrderType = null;
            }
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        orderApi = RetrofitProvider.order(); // Khởi tạo API

        setupRecyclerView();
        updateBottomSection(); // Cập nhật trạng thái ban đầu

        if (receivedOrderId != null && !receivedOrderId.isEmpty()) {
            loadData(receivedOrderId);
        } else {
            Toast.makeText(getContext(), "Không tìm thấy ID đơn hàng.", Toast.LENGTH_SHORT).show();
            binding.btnCancelOrderDetail.setVisibility(View.GONE);
        }
    }

    private void loadData(String orderId) {
        orderApi.getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> res) {
                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindData(res.body().data);
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void bindData(OrderDetailResponse.Data data) {
        try {
            if (data.order == null) return;

            // danh sách sản phẩm
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
            if (orderItemAdapter != null) orderItemAdapter.notifyDataSetChanged();

            // Thông tin tiền & ID
            binding.tvShippingFee.setText(formatVnd(data.order.orderShippingFee));
            binding.tvTotalAmount.setText(formatVnd(data.order.orderTotalAmount));

            // Payment Method
            String methodCode = data.order.orderPaymentMethod;
            String methodText = "Thanh toán điện tử";
            if (methodCode != null) {
                switch (methodCode.toLowerCase()) {
                    case "cod": methodText = "Thanh toán khi nhận hàng (COD)"; break;
                    case "zalopay": methodText = "Ví điện tử ZaloPay"; break;
                    case "wallet": methodText = "Ví nội bộ (SecondChance)"; break;
                    case "bank": methodText = "Chuyển khoản ngân hàng"; break;
                }
            }
            binding.tvPaymentMethod.setText(methodText);

            if (data.order.orderShippingAddress != null) {
                var a = data.order.orderShippingAddress;
                binding.tvReceiverName.setText(safe(a.name));
                binding.tvReceiverPhone.setText(safe(a.phone));

                String finalAddress;

                if (a.fullAddress != null && !a.fullAddress.isEmpty()) {
                    finalAddress = a.fullAddress;
                } else {

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

            if (data.order.orderStatus == 3) {
                receivedOrderType = Order.OrderType.CONFIRMED_AUCTION;
            } else if (data.shipment != null) {
                receivedOrderType = Order.OrderType.CONFIRMED_FIXED;
            } else {
                receivedOrderType = Order.OrderType.UNCONFIRMED;
            }

            updateBottomSection();

        } catch (Exception e) {
            Log.e(TAG, "Lỗi bindData: " + e.getMessage());
        }
    }

    private void setupRecyclerView() {
        orderItemAdapter = new OrderItemAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(orderItemAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
    }

    private void updateBottomSection() {
        if (binding == null) return;

        binding.btnCancelOrderDetail.setVisibility(View.GONE);
        binding.cvInfoConfirmedFixed.setVisibility(View.GONE);
        binding.cvInfoAuction.setVisibility(View.GONE);

        if (receivedOrderType != null) {
            switch (receivedOrderType) {
                case UNCONFIRMED:
                    binding.btnCancelOrderDetail.setVisibility(View.VISIBLE);
                    binding.btnCancelOrderDetail.setOnClickListener(v -> {
                        ConfirmCancelDialog dialog = new ConfirmCancelDialog(receivedOrderId, this);
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
        }
    }

    @Override
    public void onCancelConfirmed(String orderId) {
        orderApi.cancelOrder(orderId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> res) {
                if (res.isSuccessful() && res.body() != null && res.body().success) {
                    showSuccessDialog();
                } else {
                    Toast.makeText(getContext(), "Hủy đơn thất bại. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog() {
        CancelSuccessDialog successDialog = new CancelSuccessDialog(this);
        successDialog.show(getParentFragmentManager(), CancelSuccessDialog.TAG);
    }

    @Override
    public void onSuccessfulDismiss() {
        sharedViewModel.refreshOrderLists();
        sharedViewModel.requestTabChange(3);
        try {
            NavController navController = Navigation.findNavController(requireView());
            navController.popBackStack();
        } catch (Exception e) {
            Log.e(TAG, "Nav error: " + e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
