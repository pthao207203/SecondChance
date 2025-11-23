package com.example.secondchance.ui.checkout;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
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
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentCheckoutBinding;
import com.example.secondchance.dto.request.PaymentRequest;
import com.example.secondchance.dto.request.PreviewOrderRequest;
import com.example.secondchance.dto.response.PaymentResponse;
import com.example.secondchance.dto.response.PreviewOrderResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;

    private List<CartApi.CartItem> checkoutItems = new ArrayList<>();

    private long serverShippingFee = 0;
    private long serverGrandTotal = 0;

    private enum PaymentMethod { COD, WALLET, ZALOPAY }
    private PaymentMethod currentPaymentMethod = PaymentMethod.COD;

    private CheckoutProductsAdapter productsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        updatePaymentUI();
        calculateDeliveryDate();

        handleArguments();

        loadOrderPreview();

        binding.btnPaymentMethod.setOnClickListener(v -> showPaymentMethodDialog());
        binding.btnBuyNow.setOnClickListener(v -> handleBuyNow());
    }

    private void setupRecyclerView() {
        productsAdapter = new CheckoutProductsAdapter();

        binding.rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.rvProducts.setHasFixedSize(true);

        binding.rvProducts.setAdapter(productsAdapter);
    }

    private void calculateDeliveryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 5);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // binding.tvDeliveryDate.setText("Nhận vào ngày " + sdf.format(calendar.getTime()));
        // (Nhớ thêm ID tvDeliveryDate vào XML nếu muốn hiện)
    }

    private void handleArguments() {
        if (getArguments() == null) return;

        if (getArguments().containsKey("selectedItems")) {
            ArrayList<CartApi.CartItem> items = (ArrayList<CartApi.CartItem>) getArguments().getSerializable("selectedItems");
            if (items != null) checkoutItems.addAll(items);
        } else if (getArguments().containsKey("productId")) {
            String pId = getArguments().getString("productId");
            int qty = getArguments().getInt("quantity", 1);
            // Tạo item giả lập cho trường hợp mua ngay
            CartApi.CartItem item = new CartApi.CartItem();
            item.productId = pId;
            item.qty = qty;
            checkoutItems.add(item);
        }
    }

    private void loadOrderPreview() {
        binding.tvDeliveryDate.setText("Đang tính toán...");
        binding.btnBuyNow.setEnabled(false);
        binding.btnBuyNow.setAlpha(0.5f);

        PreviewOrderRequest request = new PreviewOrderRequest();
        List<PreviewOrderRequest.Item> items = new ArrayList<>();

        for (CartApi.CartItem item : checkoutItems) {
            String pId = (item.productId != null) ? item.productId :
                    (item.product != null ? item.product.id : null);

            if (pId != null) {
                items.add(new PreviewOrderRequest.Item(pId, item.qty));
            }
        }

        android.util.Log.d("CHECKOUT", "Sending items to preview: " + items.size());

        request.setItems(items);

        RetrofitProvider.order().previewOrder(request).enqueue(new Callback<PreviewOrderResponse>() {
            @Override
            public void onResponse(Call<PreviewOrderResponse> call, Response<PreviewOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().success) {
                        PreviewOrderResponse.Data data = response.body().getData();

                        serverShippingFee = data.getShippingFee();
                        serverGrandTotal = data.getGrandTotal();

                        // Cập nhật UI
                        DecimalFormat formatter = new DecimalFormat("#,###");
                        binding.tvTrasnportFee.setText(formatter.format(serverShippingFee));
                        binding.tvTotalPrice.setText(formatter.format(serverGrandTotal));

                        // Cập nhật trạng thái vận chuyển
                        binding.tvDeliveryDate.setText("Đã tính xong");

                        // Update list adapter
                        if(productsAdapter != null) {
                            productsAdapter.setItems(data.getItems());
                        }

                        // Mở khóa nút mua
                        binding.btnBuyNow.setEnabled(true);
                        binding.btnBuyNow.setAlpha(1.0f);
                    } else {
                        Toast.makeText(getContext(), "Lỗi: Không thể tính tiền đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    android.util.Log.e("CHECKOUT", "Preview Failed: " + response.code());
                    try {
                        String errorBody = response.errorBody().string();
                        android.util.Log.e("CHECKOUT", "Error Body: " + errorBody);
                        Toast.makeText(getContext(), "Lỗi tính tiền: " + response.message(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }

            @Override
            public void onFailure(Call<PreviewOrderResponse> call, Throwable t) {
                android.util.Log.e("CHECKOUT", "Network Error: " + t.getMessage());
                Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                binding.tvDeliveryDate.setText("Lỗi mạng");
            }
        });
    }

    private void showPaymentMethodDialog() {
        final BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(requireContext());
        title.setText("Chọn phương thức thanh toán");
        title.setTextSize(18);
        title.setPadding(0, 0, 0, 30);
        title.setTextColor(Color.BLACK);
        layout.addView(title);

        layout.addView(createPaymentOptionView(dialog, "Thanh toán khi nhận hàng", R.drawable.ic_credit_card, PaymentMethod.COD));
        layout.addView(createPaymentOptionView(dialog, "Ví của tôi", R.drawable.ic_profile1, PaymentMethod.WALLET));
        layout.addView(createPaymentOptionView(dialog, "Thanh toán qua ZaloPay", R.drawable.ic_transport, PaymentMethod.ZALOPAY));

        dialog.setContentView(layout);
        dialog.show();
    }

    private View createPaymentOptionView(BottomSheetDialog dialog, String name, int iconRes, PaymentMethod method) {
        LinearLayout ll = new LinearLayout(requireContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(0, 30, 0, 30);
        ll.setGravity(Gravity.CENTER_VERTICAL);
        ll.setClickable(true);

        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setLayoutParams(new LinearLayout.LayoutParams(60, 60));

        TextView tv = new TextView(requireContext());
        tv.setText(name);
        tv.setTextSize(16);
        tv.setPadding(30, 0, 0, 0);
        tv.setTextColor(Color.BLACK);

        ll.addView(icon);
        ll.addView(tv);

        ll.setOnClickListener(v -> {
            currentPaymentMethod = method;
            updatePaymentUI();
            dialog.dismiss();
        });
        return ll;
    }

    private void updatePaymentUI() {
        switch (currentPaymentMethod) {
            case COD:
                binding.tvPaymentMethodName.setText("Thanh toán khi nhận hàng");
                binding.tvPaymentDesc.setText("Thanh toán bằng tiền mặt khi nhận hàng.");
                binding.ivPaymentIcon.setImageResource(R.drawable.ic_credit_card);
                break;
            case WALLET:
                binding.tvPaymentMethodName.setText("Ví của tôi");
                binding.tvPaymentDesc.setText("Sử dụng số dư trong ví ứng dụng.");
                binding.ivPaymentIcon.setImageResource(R.drawable.ic_profile1);
                break;
            case ZALOPAY:
                binding.tvPaymentMethodName.setText("ZaloPay");
                binding.tvPaymentDesc.setText("Thanh toán an toàn qua ứng dụng ZaloPay.");
                binding.ivPaymentIcon.setImageResource(R.drawable.ic_transport);
                break;
        }
    }

    private void handleBuyNow() {
        if (checkoutItems.isEmpty()) {
            Toast.makeText(getContext(), "Không có sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPaymentMethod == PaymentMethod.ZALOPAY) {
            createZaloPayOrder();
        } else {
            Toast.makeText(requireContext(), "Tính năng đang phát triển...", Toast.LENGTH_SHORT).show();
        }
    }

    private void createZaloPayOrder() {
        binding.btnBuyNow.setEnabled(false);
        binding.btnBuyNow.setText("Đang tạo đơn...");

        List<PaymentRequest.Item> requestItems = new ArrayList<>();
        for (CartApi.CartItem item : checkoutItems) {
            String pId = (item.productId != null) ? item.productId :
                    (item.product != null ? item.product.id : null);
            if(pId != null) requestItems.add(new PaymentRequest.Item(pId, item.qty));
        }

        PaymentRequest.ShippingAddress address = new PaymentRequest.ShippingAddress();
        address.setFullName("Khách Hàng");
        address.setPhone("0909123456");
        address.setAddress("TP.HCM");

        PaymentRequest request = new PaymentRequest();
        request.setItems(requestItems);
        request.setShippingAddress(address);

        request.setShippingFee(serverShippingFee);

        RetrofitProvider.payment().createZaloPayUrl(request).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                binding.btnBuyNow.setEnabled(true);
                binding.btnBuyNow.setText("MUA NGAY");

                if (response.isSuccessful() && response.body() != null) {
                    String payUrl = response.body().getPayUrl();
                    if (payUrl != null && !payUrl.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Lỗi: Link thanh toán rỗng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Tạo đơn thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                binding.btnBuyNow.setEnabled(true);
                binding.btnBuyNow.setText("MUA NGAY");
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
