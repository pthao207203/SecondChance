package com.example.secondchance.ui.order;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.secondchance.databinding.FragmentDeliveringOrderDetailBinding;
import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.TrackingStatus;
import com.example.secondchance.data.model.OrderItem;
import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import com.example.secondchance.ui.order.adapter.TrackingStatusAdapter;
import com.example.secondchance.viewmodel.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

public class DeliveringOrderDetailFragment extends Fragment {
    private static final String TAG = "DeliveringDetailFrag";
    private FragmentDeliveringOrderDetailBinding binding;
    private String receivedOrderId;
    private List<TrackingStatus> trackingList = new ArrayList<>();
    private TrackingStatusAdapter trackingAdapter;
    private Order.DeliveryOverallStatus receivedDeliveryStatus;
    private OrderItemAdapter productAdapter;
    private List<OrderItem> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDeliveringOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            try {
                receivedDeliveryStatus = (Order.DeliveryOverallStatus) getArguments().getSerializable("deliveryStatus");
            } catch (Exception e) {
                Log.e(TAG, "Error getting deliveryStatus from arguments", e);
                receivedDeliveryStatus = Order.DeliveryOverallStatus.PACKAGED;
            }
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView cho trạng thái vận chuyển
        setupTrackingRecyclerView();

        // RecyclerView cho danh sách sản phẩm
        setupProductRecyclerView();

        // Tải dữ liệu chi tiết
        if (receivedOrderId != null) {
            loadDeliveringOrderDetails(receivedOrderId);
        } else {
            Log.e(TAG, "Order ID is null.");
            Toast.makeText(getContext(), "Lỗi tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }

        // Xử lý nút "ĐÃ NHẬN HÀNG"
        binding.btnReceiveOrder.setOnClickListener(v -> showConfirmReceiptDialog());
    }

     //Hiển thị dialog xác nhận "ĐÃ NHẬN HÀNG"

    private void showConfirmReceiptDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_confirm_receipt, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        Button btnCancel = dialogView.findViewById(R.id.btnConfirmCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnKeepOrder);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            // Hiển thị dialog cảm ơn sau khi xác nhận
            showAfterConfirmDialog();

            // Cập nhật trạng thái đơn hàng UI
            updateStepper(Order.DeliveryOverallStatus.DELIVERED);
            binding.btnReceiveOrder.setVisibility(View.GONE);

            // TODO: Gọi API backend để cập nhật trạng thái "DELIVERED"
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }


     //Hiển thị dialog cảm ơn sau khi xác nhận đã nhận hàng
     //Khi bấm "Để sau" -> sẽ chuyển sang tab "Đã mua" (index = 2)
    private void showAfterConfirmDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_after_confirm_receipt, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        Button btnLater = dialogView.findViewById(R.id.btnConfirmCancel);
        Button btnReview = dialogView.findViewById(R.id.btnKeepOrder);

        // Nút "Để sau" -> quay về danh sách đơn và chuyển tab "Đã mua"
        btnLater.setOnClickListener(v -> {
            dialog.dismiss();

            // Gửi yêu cầu chuyển tab tới SharedViewModel
            SharedViewModel vm = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            int targetTabIndex = 2; //

            try {
                vm.requestTabChange(targetTabIndex);
            } catch (Exception e) {
                Log.w(TAG, "Failed to request tab change via SharedViewModel: " + e.getMessage());
            }

            // Quay về fragment chứa ViewPager (StatusOrderFragment)
            try {
                NavHostFragment.findNavController(this).popBackStack();
            } catch (Exception e) {
                Log.w(TAG, "Failed to pop back stack: " + e.getMessage());
            }
        });

        // "Đánh giá" -> hiện Toast và (tuỳ bạn) điều hướng tới màn hình đánh giá
        btnReview.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Chuyển đến màn hình đánh giá...", Toast.LENGTH_SHORT).show();

        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    // RecyclerView SẢN PHẨM
    private void setupProductRecyclerView() {
        productAdapter = new OrderItemAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);

        Log.d(TAG, "Product RecyclerView setup complete.");
    }

    // tải dữ liệu chi tiết
    private void loadDeliveringOrderDetails(String orderId) {
        Log.d(TAG, "Placeholder: Load delivering order details for " + orderId);

        // Cập nhật Stepper theo trạng thái nhận được
        if (receivedDeliveryStatus != null) {
            updateStepper(receivedDeliveryStatus);
        } else {
            updateStepper(Order.DeliveryOverallStatus.PACKAGED);
        }

        if (receivedDeliveryStatus == Order.DeliveryOverallStatus.DELIVERING) {
            // CHỈ HIỆN NÚT KHI "ĐANG GIAO"
            binding.btnReceiveOrder.setVisibility(View.VISIBLE);
        } else {
            // ẨN NÚT VỚI MỌI TRẠNG THÁI KHÁC
            binding.btnReceiveOrder.setVisibility(View.GONE);
        }

        // CẬP NHẬT DANH SÁCH SẢN PHẨM
        loadDummyProductData();
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product list updated for RecyclerView");
        }

        // Cập nhật danh sách tracking
        loadTrackingDataBasedOnStatus(receivedDeliveryStatus);
        if (trackingAdapter != null) {
            trackingAdapter.notifyDataSetChanged();
            Log.d(TAG, "Tracking list updated");
        }
    }

    private void loadDummyProductData() {
        productList.clear();
        productList.add(new OrderItem(R.drawable.nhan1, "Nhẫn Kim Cương Hữu Hạn", "Loại 1, Hãng abc thành phố Xuân Hợp", "50.000"));
        productList.add(new OrderItem(R.drawable.sample_flower, "Vòng Tay Vàng 24K", "Giỏ hoa loại 1 new 99%", "150.000"));
    }

    private void loadTrackingDataBasedOnStatus(Order.DeliveryOverallStatus status) {
        trackingList.clear();
        List<TrackingStatus> temp = new ArrayList<>();

        if (status.ordinal() >= Order.DeliveryOverallStatus.PACKAGED.ordinal()) {
            temp.add(new TrackingStatus("10:00, 25/10/2025", "Gói hàng đã được đóng gói", false));
        }
        if (status.ordinal() >= Order.DeliveryOverallStatus.AT_POST_OFFICE.ordinal()) {
            temp.add(new TrackingStatus("15:30, 25/10/2025", "Đã đến bưu cục ABC", false));
        }
        if (status.ordinal() >= Order.DeliveryOverallStatus.DELIVERING.ordinal()) {
            temp.add(new TrackingStatus("08:00, 26/10/2025", "Đang trên đường giao đến bạn", false));
        }
        if (status.ordinal() >= Order.DeliveryOverallStatus.DELIVERED.ordinal()) {
            temp.add(new TrackingStatus("XX:XX, XX/XX/2025", "Giao hàng thành công", false));
        }

        trackingList.addAll(temp);

        if (!trackingList.isEmpty()) {
            trackingList.get(trackingList.size() - 1).setActive(true);
        }
    }

    private void setupTrackingRecyclerView() {
        trackingAdapter = new TrackingStatusAdapter(getContext(), trackingList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        binding.rvTrackingStatus.setLayoutManager(layoutManager);
        binding.rvTrackingStatus.setAdapter(trackingAdapter);
        binding.rvTrackingStatus.setNestedScrollingEnabled(false);

        Log.d(TAG, "Tracking RecyclerView setup complete (Reverse Layout).");
    }

    private void updateStepper(Order.DeliveryOverallStatus status) {
        if (binding == null || status == null || getContext() == null) return;

        Log.d(TAG, "Updating stepper for status: " + status);

        ImageView step1Icon = binding.stepperLayout.findViewById(R.id.step1_icon);
        ImageView step2Icon = binding.stepperLayout.findViewById(R.id.step2_icon);
        ImageView step3Icon = binding.stepperLayout.findViewById(R.id.step3_icon);
        ImageView step4Icon = binding.stepperLayout.findViewById(R.id.step4_icon);

        View step1Line = binding.stepperLayout.findViewById(R.id.step1_line);
        View step2Line = binding.stepperLayout.findViewById(R.id.step2_line);
        View step3Line = binding.stepperLayout.findViewById(R.id.step3_line);

        TextView step1Label = binding.stepperLayout.findViewById(R.id.step1_label);
        TextView step2Label = binding.stepperLayout.findViewById(R.id.step2_label);
        TextView step3Label = binding.stepperLayout.findViewById(R.id.step3_label);
        TextView step4Label = binding.stepperLayout.findViewById(R.id.step4_label);

        int activeColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveColor = ContextCompat.getColor(requireContext(), R.color.highLight4);
        int activeTextColor = ContextCompat.getColor(requireContext(), R.color.highLight5);
        int inactiveTextColor = ContextCompat.getColor(requireContext(), R.color.text_secondary);
        int activeIcon = R.drawable.ic_active;
        int inactiveIcon = R.drawable.ic_inactive;

        // Reset
        step1Icon.setBackgroundResource(inactiveIcon);
        step2Icon.setBackgroundResource(inactiveIcon);
        step3Icon.setBackgroundResource(inactiveIcon);
        step4Icon.setBackgroundResource(inactiveIcon);
        step1Line.setBackgroundColor(inactiveColor);
        step2Line.setBackgroundColor(inactiveColor);
        step3Line.setBackgroundColor(inactiveColor);
        step1Label.setTextColor(inactiveTextColor);
        step2Label.setTextColor(inactiveTextColor);
        step3Label.setTextColor(inactiveTextColor);
        step4Label.setTextColor(inactiveTextColor);

        // Kích hoạt theo trạng thái
        switch (status) {
            case DELIVERED:
                step4Icon.setBackgroundResource(activeIcon);
                step4Label.setTextColor(activeTextColor);
                step3Line.setBackgroundColor(activeColor);
            case DELIVERING:
                step3Icon.setBackgroundResource(activeIcon);
                step3Label.setTextColor(activeTextColor);
                step2Line.setBackgroundColor(activeColor);
            case AT_POST_OFFICE:
                step2Icon.setBackgroundResource(activeIcon);
                step2Label.setTextColor(activeTextColor);
                step1Line.setBackgroundColor(activeColor);
            case PACKAGED:
                step1Icon.setBackgroundResource(activeIcon);
                step1Label.setTextColor(activeTextColor);
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }
}
