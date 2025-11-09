//package com.example.secondchance.ui.shoporder;
//
//import android.annotation.SuppressLint;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//import androidx.navigation.Navigation;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
////import com.example.secondchance.data.remote.ShopOrderApi;
//import com.example.secondchance.data.remote.RetrofitProvider;
////import com.example.secondchance.databinding.FragmentBoughtShopOrderDetailBinding;
//import com.example.secondchance.R;
//import com.example.secondchance.data.model.ShopOrderItem;
////import com.example.secondchance.dto.response.ShopOrderDetailResponse;
//import com.example.secondchance.ui.shoporder.dialog.RefundConfirmDialogShopFragment;
//import com.example.secondchance.ui.shoporder.adapter.ShopOrderItemAdapter;
//import com.example.secondchance.data.model.TrackingStatus;
//import com.example.secondchance.ui.order.adapter.TrackingStatusAdapter;
//
//import java.text.NumberFormat;
//import java.time.ZoneId;
//import java.time.ZonedDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class BoughtShopOrderDetailFragment extends Fragment implements RefundConfirmDialogShopFragment.RefundConfirmListener {
//    private static final String TAG = "BoughtShopDetailFrag";
//    private FragmentBoughtShopOrderDetailBinding binding;
//    private String receivedOrderId;
//    private boolean receivedIsEvaluated;
//    private ShopOrderItemAdapter productAdapter;
//    private List<ShopOrderItem> productList = new ArrayList<>();
//    private TrackingStatusAdapter trackingAdapter;
//    private List<TrackingStatus> trackingList = new ArrayList<>();
//    private ShopOrderApi shopOrderApi;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        binding = FragmentBoughtShopOrderDetailBinding.inflate(inflater, container, false);
//
//        if (getArguments() != null) {
//            receivedOrderId = getArguments().getString("orderId");
//            receivedIsEvaluated = getArguments().getBoolean("isEvaluated");
//        }
//
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // Recycler: sản phẩm
//        productAdapter = new ShopOrderItemAdapter(requireContext(), productList);
//        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(requireContext()));
//        binding.rvOrderItems.setAdapter(productAdapter);
//        binding.rvOrderItems.setNestedScrollingEnabled(false);
//
//        // (Nếu cần dùng timeline tracking thì mở ra)
//        // trackingAdapter = new TrackingStatusAdapter(trackingList);
//        // binding.rvTracking.setLayoutManager(new LinearLayoutManager(requireContext()));
//        // binding.rvTracking.setAdapter(trackingAdapter);
//
//        updateBottomButtons();
//
//        binding.btnReturnOrder.setOnClickListener(v -> showRefundConfirmDialog());
//
//        // Gọi API
//        if (receivedOrderId == null || receivedOrderId.isEmpty()) {
//            Toast.makeText(requireContext(), "Thiếu orderId", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        shopOrderApi = RetrofitProvider.shopOrder();
//        loadOrderDetail(receivedOrderId);
//    }
//
//    private void loadOrderDetail(String id) {
//        showLoading(true);
//        shopOrderApi.getShopOrderDetail(id).enqueue(new Callback<ShopOrderDetailResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<ShopOrderDetailResponse> call,
//                                   @NonNull Response<ShopOrderDetailResponse> res) {
//                showLoading(false);
//                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
//                    Toast.makeText(requireContext(), "Không tải được chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                bindOrder(res.body().data);
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ShopOrderDetailResponse> call, @NonNull Throwable t) {
//                showLoading(false);
//                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
//    private void bindOrder(ShopOrderDetailResponse.Data data) {
//        // 1) Sản phẩm
//        productList.clear();
//        if (data.order != null && data.order.orderItems != null) {
//            for (ShopOrderDetailResponse.OrderItem it : data.order.orderItems) {
//                productList.add(new ShopOrderItem(
//                        it.imageUrl,
//                        safe(it.name),
//                        "SL: " + it.qty,
//                        formatVnd(it.price)
//                ));
//            }
//        }
//        productAdapter.notifyDataSetChanged();
//
//        // 2) Tổng tiền + địa chỉ + trạng thái
//        if (data.order != null) {
//            binding.tvOrderId.setText("#" + data.order.id.toUpperCase(Locale.ROOT));
//            binding.tvShippingFee.setText(formatVnd(data.order.orderShippingFee));
//            binding.tvTotalAmount.setText(formatVnd(data.order.orderTotalAmount));
//            binding.tvPaymentMethod.setText(safe(data.order.orderPaymentMethod).equals("cod") ? "Tiền mặt" : "Ví của tôi");
//
//            if (data.order.orderShippingAddress != null) {
//                var a = data.order.orderShippingAddress;
//                binding.tvReceiverName.setText(safe(a.name));
//                binding.tvReceiverPhone.setText(safe(a.phone));
//                binding.tvReceiverAddress.setText(safe(a.street) + ", " + safe(a.ward) + ", " + safe(a.province));
//            }
//        }
//
//        // 3) Timeline vận chuyển (nếu có)
//        // trackingList.clear();
//        // if (data.shipment != null && data.shipment.events != null) {
//        //     for (ShopOrderDetailResponse.Event e : data.shipment.events) {
//        //         trackingList.add(new TrackingStatus(
//        //                 mapStatusTitle(e),
//        //                 formatVnTime(e.eventTime),
//        //                 safe(e.description)
//        //         ));
//        //     }
//        //     Collections.sort(trackingList, Comparator.comparing(ts -> ts.getTime()));
//        // }
//        // trackingAdapter.notifyDataSetChanged();
//    }
//
//    private String mapStatusTitle(ShopOrderDetailResponse.Event e) {
//        if (e == null) return "Cập nhật";
//        if (e.eventCode == null) return upperFirst(safe(e.description));
//        switch (e.eventCode) {
//            case 1: return "Nhận hàng từ shop";
//            case 2: return "Đến kho";
//            case 3: return "Rời kho";
//            case 4: return "Phát/Hoàn trả";
//            case 5: return "Đã giao";
//            default: return "Cập nhật";
//        }
//    }
//
//    private void showRefundConfirmDialog() {
//        RefundConfirmDialogShopFragment dialog = new RefundConfirmDialogShopFragment();
//        dialog.setListener(this);
//        dialog.show(getParentFragmentManager(), "RefundConfirmDialogTag");
//    }
//
//    @Override
//    public void onRefundConfirmed() {
//        Toast.makeText(requireContext(), "Đã xác nhận. Chuyển sang màn hình tạo yêu cầu hoàn trả...", Toast.LENGTH_LONG).show();
//
//        Bundle bundle = new Bundle();
//        if (receivedOrderId != null) {
//            bundle.putString("orderId", receivedOrderId);
//        }
//
//        try {
//            Navigation.findNavController(requireView()).navigate(
//                    R.id.action_boughtShopOrderDetailFragment_to_createShopOrderReturnRequestFragment,
//                    bundle
//            );
//        } catch (Exception e) {
//            Log.e(TAG, "Lỗi điều hướng đến CreateShopOrderReturnRequestFragment", e);
//            Toast.makeText(requireContext(), "Lỗi: Không thể chuyển màn hình. Thiếu action điều hướng.", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    @Override
//    public void onRefundCancelled() {
//        Toast.makeText(requireContext(), "Yêu cầu hoàn trả đã bị hủy.", Toast.LENGTH_SHORT).show();
//    }
//
//    private void updateBottomButtons() {
//        if (receivedIsEvaluated) {
//            binding.btnRateShop.setVisibility(View.GONE);
//            Log.d(TAG, "Order đã đánh giá. Ẩn nút Đánh giá.");
//        } else {
//            binding.btnRateShop.setVisibility(View.VISIBLE);
//            Log.d(TAG, "Order chưa đánh giá. Hiển thị nút Đánh giá.");
//
//            binding.btnRateShop.setOnClickListener(v -> {
//                Log.d(TAG, "Rate Shop clicked for order: " + receivedOrderId);
//                Toast.makeText(getContext(), "Mở màn hình Đánh giá...", Toast.LENGTH_SHORT).show();
//            });
//        }
//    }
//
//    private void showLoading(boolean show) {
//        binding.getRoot().setAlpha(show ? 0.6f : 1f);
//    }
//
//    private String formatVnd(long amount) {
//        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
//    }
//
//    private String formatVnTime(String iso) {
//        try {
//            ZonedDateTime z = ZonedDateTime.parse(iso).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
//            return z.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
//        } catch (Exception e) {
//            return iso;
//        }
//    }
//
//    private String safe(String s) {
//        return s == null ? "" : s;
//    }
//
//    private String upperFirst(String s) {
//        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//}
