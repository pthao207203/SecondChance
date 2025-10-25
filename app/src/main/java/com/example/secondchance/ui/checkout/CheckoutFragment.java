package com.example.secondchance.ui.checkout;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.example.secondchance.R;
import com.example.secondchance.ui.cart.CartItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutFragment extends Fragment {

    private ViewPager2 viewPagerProducts;
    private LinearLayout indicatorLayout;
    private TextView tvTotalPrice;
    private View btnBuyNow;
    private View btnShippingMethod, btnPaymentMethod, btnShippingAddress;
    private CheckoutProductsAdapter productsAdapter;
    private List<CartItem> selectedProducts;
    private int shippingFee = 15000;
    private int totalAmount = 0;

    private BottomSheetDialog qrPaymentDialog;
    private CountDownTimer countDownTimer;
    private long remainingTime = 900000; // 15 phút

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Nhận dữ liệu từ CartFragment
        if (getArguments() != null) {
            selectedProducts = (List<CartItem>) getArguments().getSerializable("selectedItems");
        }

        if (selectedProducts == null) {
            selectedProducts = new ArrayList<>();
        }

        // Khởi tạo views
        initViews(view);

        // Thiết lập ViewPager
        setupViewPager();

        // Thiết lập các sự kiện click
        setupClickListeners();

        // Cập nhật tổng giá
        updateTotalPrice();
    }

    private void initViews(View view) {
        viewPagerProducts = view.findViewById(R.id.viewPagerProducts);
        indicatorLayout = view.findViewById(R.id.indicatorLayout);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnBuyNow = view.findViewById(R.id.btnBuyNow);
        btnShippingMethod = view.findViewById(R.id.btnShippingMethod);
        btnPaymentMethod = view.findViewById(R.id.btnPaymentMethod);
        btnShippingAddress = view.findViewById(R.id.btnShippingAddress);
    }

    private void setupViewPager() {
        productsAdapter = new CheckoutProductsAdapter(selectedProducts);
        viewPagerProducts.setAdapter(productsAdapter);

        // Thiết lập indicator
        setupIndicators(selectedProducts.size());

        // Cập nhật indicator khi page thay đổi
        viewPagerProducts.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });
    }

    private void setupIndicators(int count) {
        indicatorLayout.removeAllViews();

        if (count <= 1) {
            indicatorLayout.setVisibility(View.GONE);
            return;
        }

        indicatorLayout.setVisibility(View.VISIBLE);

        View[] indicators = new View[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < count; i++) {
            indicators[i] = new View(requireContext());
            indicators[i].setLayoutParams(params);
            indicators[i].setBackgroundResource(R.drawable.indicator_inactive);
            indicatorLayout.addView(indicators[i]);
        }

        // Set first indicator as active
        if (count > 0) {
            indicators[0].setBackgroundResource(R.drawable.indicator_active);
        }
    }

    private void updateIndicators(int position) {
        int childCount = indicatorLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View indicator = indicatorLayout.getChildAt(i);
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.indicator_active);
            } else {
                indicator.setBackgroundResource(R.drawable.indicator_inactive);
            }
        }
    }

    private void setupClickListeners() {
        // Nút mua ngay - Hiển thị dialog QR payment
        btnBuyNow.setOnClickListener(v -> {
            remainingTime = 900000; // Reset về 15 phút
            showQRPaymentDialog();
        });

        // Phương thức vận chuyển
        btnShippingMethod.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Chọn phương thức vận chuyển", Toast.LENGTH_SHORT).show()
        );

        // Phương thức thanh toán
        btnPaymentMethod.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
        );

        // Địa chỉ nhận hàng
        btnShippingAddress.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Chỉnh sửa địa chỉ nhận hàng", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateTotalPrice() {
        totalAmount = shippingFee;

        for (CartItem item : selectedProducts) {
            totalAmount += item.getPrice();
        }

        tvTotalPrice.setText("đ " + String.format("%,d", totalAmount));
    }

    private void showQRPaymentDialog() {
        qrPaymentDialog = new BottomSheetDialog(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qr_payment, null);
        qrPaymentDialog.setContentView(dialogView);
        qrPaymentDialog.setCancelable(false);

        // Khởi tạo views trong dialog
        ImageView btnClose = dialogView.findViewById(R.id.btnClose);
        TextView tvCountdown = dialogView.findViewById(R.id.tvCountdown);
        TextView tvAmount = dialogView.findViewById(R.id.tvAmount);
        View btnConfirmPayment = dialogView.findViewById(R.id.btnConfirmPayment);

        // Set amount
        tvAmount.setText("đ " + String.format("%,d", totalAmount));

        // Countdown timer
        startCountdown(tvCountdown);

        // Nút đóng - Hiển thị dialog xác nhận hủy
        btnClose.setOnClickListener(v -> {
            stopCountdown();
            qrPaymentDialog.dismiss();
            showCancelPaymentDialog();
        });

        // Nút xác nhận thanh toán
        btnConfirmPayment.setOnClickListener(v -> {
            stopCountdown();
            qrPaymentDialog.dismiss();
            showPaymentSuccessDialog();
        });

        // Khi dialog bị đóng
        qrPaymentDialog.setOnDismissListener(dialog -> stopCountdown());

        qrPaymentDialog.show();
    }

    private void startCountdown(TextView tvCountdown) {
        countDownTimer = new CountDownTimer(remainingTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTime = millisUntilFinished;
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                String timeString = String.format(Locale.getDefault(),
                        "%02d phút %02d giây", minutes, seconds);
                tvCountdown.setText(timeString);
            }

            @Override
            public void onFinish() {
                // Hết thời gian - Tự động hủy thanh toán
                if (qrPaymentDialog != null && qrPaymentDialog.isShowing()) {
                    qrPaymentDialog.dismiss();
                    showPaymentTimeoutDialog();
                }
            }
        };
        countDownTimer.start();
    }

    private void stopCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void showCancelPaymentDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cancel_payment);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.7f);
        dialog.setCancelable(false);

        MaterialButton btnConfirmCancel = dialog.findViewById(R.id.btnConfirmCancel);
        MaterialButton btnAbortCancel = dialog.findViewById(R.id.btnAbortCancel);

        // Xác nhận hủy - Hiển thị dialog hủy thành công
        btnConfirmCancel.setOnClickListener(v -> {
            dialog.dismiss();
            remainingTime = 900000; // Reset thời gian
            showCancelSuccessDialog();
        });

        // Hủy việc hủy - Quay lại QR dialog và tiếp tục đếm ngược
        btnAbortCancel.setOnClickListener(v -> {
            dialog.dismiss();
            showQRPaymentDialog(); // Mở lại QR dialog với thời gian còn lại
        });

        dialog.show();
    }

    private void showCancelSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cancel_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.7f);

        ImageView btnCloseSuccess = dialog.findViewById(R.id.btnCloseSuccess);
        btnCloseSuccess.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        });

        // Auto dismiss after 2 seconds
        requireView().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                Toast.makeText(requireContext(), "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        }, 2000);

        dialog.show();
    }

    private void showPaymentTimeoutDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_timeout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.7f);
        dialog.setCancelable(false);

        MaterialButton btnGoHome = dialog.findViewById(R.id.btnGoHome);
        MaterialButton btnRetry = dialog.findViewById(R.id.btnRetry);

        // Về trang chủ
        btnGoHome.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Đã hết thời gian thanh toán", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        });

        // Thử lại - Reset thời gian về 15 phút
        btnRetry.setOnClickListener(v -> {
            dialog.dismiss();
            remainingTime = 900000; // Reset về 15 phút
            showQRPaymentDialog();
        });

        dialog.show();
    }

    private void showPaymentSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_payment_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.7f);
        dialog.setCancelable(false);

        ImageView btnCloseSuccess = dialog.findViewById(R.id.btnCloseSuccess);
        btnCloseSuccess.setOnClickListener(v -> {
            dialog.dismiss();
            Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        });

        // Auto dismiss after 3 seconds
        requireView().postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                Toast.makeText(requireContext(), "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        }, 3000);

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCountdown();
        if (qrPaymentDialog != null && qrPaymentDialog.isShowing()) {
            qrPaymentDialog.dismiss();
        }
    }
}