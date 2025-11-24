package com.example.secondchance.ui.order;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.data.repo.CartRepository;
import com.example.secondchance.ui.order.adapter.OrderPreviewProductAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderPreviewFragment extends Fragment {

    private RecyclerView productsRecyclerView;
    private OrderPreviewProductAdapter productAdapter;
    private TextView tvTotalPrice;
    private Button btnBuyNow;

    // Section Views
    private LinearLayout shippingMethodHeader, paymentMethodHeader, addressHeader;
    private LinearLayout selectedShippingLayout, selectedPaymentLayout, selectedAddressLayout;
    private LinearLayout shippingOptionsContainer, paymentOptionsContainer, addressOptionsContainer;
    private ImageView ivShippingToggle, ivPaymentToggle, ivAddressToggle;

    // Data
    private OrderApi.Address selectedAddress;
    private OrderApi.PaymentMethod selectedPaymentMethod;
    private ArrayList<CartApi.CartItem> selectedCartItems;
    private long shippingFee = 30000; // Hardcoded for now

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_preview, container, false);
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        fetchPreviewData();
        return view;
    }

    private void initViews(View view) {
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnBuyNow = view.findViewById(R.id.btnBuyNow);

        shippingMethodHeader = view.findViewById(R.id.shippingMethodHeader);
        selectedShippingLayout = view.findViewById(R.id.selectedShippingLayout);
        shippingOptionsContainer = view.findViewById(R.id.shippingOptionsContainer);
        ivShippingToggle = view.findViewById(R.id.ivShippingToggle);

        paymentMethodHeader = view.findViewById(R.id.paymentMethodHeader);
        selectedPaymentLayout = view.findViewById(R.id.selectedPaymentLayout);
        paymentOptionsContainer = view.findViewById(R.id.paymentOptionsContainer);
        ivPaymentToggle = view.findViewById(R.id.ivPaymentToggle);

        addressHeader = view.findViewById(R.id.addressHeader);
        selectedAddressLayout = view.findViewById(R.id.selectedAddressLayout);
        addressOptionsContainer = view.findViewById(R.id.addressOptionsContainer);
        ivAddressToggle = view.findViewById(R.id.ivAddressToggle);
    }

    private void setupRecyclerView() {
        productAdapter = new OrderPreviewProductAdapter(new ArrayList<>());
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productsRecyclerView.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        shippingMethodHeader.setOnClickListener(v -> toggleVisibility(shippingOptionsContainer, selectedShippingLayout, ivShippingToggle));
        paymentMethodHeader.setOnClickListener(v -> toggleVisibility(paymentOptionsContainer, selectedPaymentLayout, ivPaymentToggle));
        addressHeader.setOnClickListener(v -> toggleVisibility(addressOptionsContainer, selectedAddressLayout, ivAddressToggle));
        btnBuyNow.setOnClickListener(v -> placeOrder());
    }

    private void toggleVisibility(View optionsContainer, View selectedLayout, ImageView toggleIcon) {
        boolean isExpanded = optionsContainer.getVisibility() == View.VISIBLE;
        optionsContainer.setVisibility(isExpanded ? View.GONE : View.VISIBLE);
        selectedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        toggleIcon.setImageResource(isExpanded ? R.drawable.chevron_up : R.drawable.chevron_down);
    }

    private void fetchPreviewData() {
        OrderApi.PreviewRequestBody requestBody = new OrderApi.PreviewRequestBody();

        if (getArguments() != null && getArguments().getSerializable("selectedItems") != null) {
            selectedCartItems = (ArrayList<CartApi.CartItem>) getArguments().getSerializable("selectedItems");
        } else {
            selectedCartItems = new ArrayList<>();
        }

        if (selectedCartItems.isEmpty()) {
            Toast.makeText(getContext(), "Không có sản phẩm nào được chọn", Toast.LENGTH_SHORT).show();
            return;
        }

        requestBody.items = selectedCartItems.stream().map(cartItem -> {
            OrderApi.CartItemInfo itemInfo = new OrderApi.CartItemInfo();
            itemInfo.productId = cartItem.productId;
            itemInfo.qty = cartItem.qty;
            return itemInfo;
        }).collect(Collectors.toCollection(ArrayList::new));


        RetrofitProvider.order().previewOrder(requestBody).enqueue(new Callback<OrderApi.OrderPreviewResponse>() {
            @Override
            public void onResponse(Call<OrderApi.OrderPreviewResponse> call, Response<OrderApi.OrderPreviewResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    OrderApi.PreviewData data = response.body().data;
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> updateUI(data));
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load preview", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderApi.OrderPreviewResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(OrderApi.PreviewData data) {
        if (data == null || getContext() == null) return;

        if (data.items != null && !data.items.isEmpty()) {
            List<OrderApi.ProductItem> allProducts = data.items.stream()
                    .flatMap(shopItems -> shopItems.items.stream())
                    .collect(Collectors.toList());
            productAdapter.updateData(allProducts);
        }

        tvTotalPrice.setText(String.format("%,d", data.totalPrice).replace(",", "."));

        updatePaymentMethods(data.paymentMethods);
        updateAddressSection(data.addresses);
        updateShippingSection();
    }

    private void updatePaymentMethods(List<OrderApi.PaymentMethod> methods) {
        paymentOptionsContainer.removeAllViews();
        if (methods == null || methods.isEmpty()) {
            paymentMethodHeader.setVisibility(View.GONE);
            selectedPaymentLayout.setVisibility(View.GONE);
            return;
        }

        selectedPaymentMethod = methods.get(0);
        updateSelectedPaymentView(selectedPaymentMethod);

        for (OrderApi.PaymentMethod method : methods) {
            View paymentOptionView = createPaymentOptionView(method, false);
            paymentOptionView.setOnClickListener(v -> {
                selectedPaymentMethod = method;
                updateSelectedPaymentView(method);
                toggleVisibility(paymentOptionsContainer, selectedPaymentLayout, ivPaymentToggle);
            });
            paymentOptionsContainer.addView(paymentOptionView);
        }
    }

    private View createPaymentOptionView(OrderApi.PaymentMethod method, boolean isSelected) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_checkout_payment_option, paymentOptionsContainer, false);
        TextView tvMethod = view.findViewById(R.id.tvPaymentMethod);
        ImageView ivIcon = view.findViewById(R.id.ivPaymentIcon);
        tvMethod.setText(method.label);

        if (isSelected) {
            ivIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.highLight5));
            view.setBackgroundResource(R.drawable.bg_normal);
        } else {
            ivIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.darkerDay));
            view.setBackgroundResource(R.drawable.bg_bottom_border_light_blue);
        }

        return view;
    }

    private void updateSelectedPaymentView(OrderApi.PaymentMethod method) {
        selectedPaymentLayout.removeAllViews();
        View selectedView = createPaymentOptionView(method, true);
        selectedPaymentLayout.addView(selectedView);
    }

    private void updateAddressSection(List<OrderApi.Address> addresses) {
        addressOptionsContainer.removeAllViews();
        if (addresses == null || addresses.isEmpty()) {
            addressHeader.setVisibility(View.GONE);
            selectedAddressLayout.setVisibility(View.GONE);
            return;
        }

        selectedAddress = addresses.stream().filter(a -> a.isDefault).findFirst().orElse(addresses.get(0));
        updateSelectedAddressView(selectedAddress);

        for (OrderApi.Address address : addresses) {
            View addressView = createAddressOptionView(address, false);
            addressView.setOnClickListener(v -> {
                selectedAddress = address;
                updateSelectedAddressView(address);
                toggleVisibility(addressOptionsContainer, selectedAddressLayout, ivAddressToggle);
            });
            addressOptionsContainer.addView(addressView);
        }
    }

    private View createAddressOptionView(OrderApi.Address address, boolean isSelected) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_address_option, addressOptionsContainer, false);
        TextView tvName = view.findViewById(R.id.tvReceiverName);
        TextView tvPhone = view.findViewById(R.id.tvReceiverPhone);
        TextView tvAddress = view.findViewById(R.id.tvReceiverAddress);
        ImageView ivProfile = view.findViewById(R.id.ivProfileIcon);
        ImageView ivPhone = view.findViewById(R.id.ivPhoneIcon);
        ImageView ivMapPin = view.findViewById(R.id.ivMapPinIcon);

        tvName.setText(address.name);
        tvPhone.setText(address.phone);
        String fullAddress = address.street + ", " + address.ward + ", " + address.province;
        tvAddress.setText(fullAddress);

        if (isSelected) {
            ivProfile.setColorFilter(ContextCompat.getColor(getContext(), R.color.highLight5));
            ivPhone.setColorFilter(ContextCompat.getColor(getContext(), R.color.highLight5));
            ivMapPin.setColorFilter(ContextCompat.getColor(getContext(), R.color.highLight5));
            view.setBackgroundResource(R.drawable.bg_normal);
        } else {
            ivProfile.setColorFilter(ContextCompat.getColor(getContext(), R.color.darkerDay));
            ivPhone.setColorFilter(ContextCompat.getColor(getContext(), R.color.darkerDay));
            ivMapPin.setColorFilter(ContextCompat.getColor(getContext(), R.color.darkerDay));
            view.setBackgroundResource(R.drawable.bg_bottom_border_light_blue);
        }

        return view;
    }

    private void updateSelectedAddressView(OrderApi.Address address) {
        selectedAddressLayout.removeAllViews();
        View selectedView = createAddressOptionView(address, true);
        selectedAddressLayout.addView(selectedView);
    }

    private void updateShippingSection() {
        List<String> shippingOptions = new ArrayList<>();
        shippingOptions.add("Vận chuyển nhanh");

        shippingOptionsContainer.removeAllViews();
        updateSelectedShippingView(shippingOptions.get(0));

        for (String option : shippingOptions) {
            View shippingView = createShippingOptionView(option, false);
            shippingView.setOnClickListener(v -> {
                updateSelectedShippingView(option);
                toggleVisibility(shippingOptionsContainer, selectedShippingLayout, ivShippingToggle);
            });
            shippingOptionsContainer.addView(shippingView);
        }
    }

    private View createShippingOptionView(String option, boolean isSelected) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_shipping_option, shippingOptionsContainer, false);
        TextView tvShippingName = view.findViewById(R.id.tvShippingName);
        ImageView ivShippingIcon = view.findViewById(R.id.ivShippingIcon);
        tvShippingName.setText(option);

        if (isSelected) {
            ivShippingIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.highLight5));
            view.setBackgroundResource(R.drawable.bg_normal);
        } else {
            ivShippingIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.darkerDay));
            view.setBackgroundResource(R.drawable.bg_bottom_border_light_blue);
        }
        return view;
    }

    private void updateSelectedShippingView(String option) {
        selectedShippingLayout.removeAllViews();
        View selectedView = createShippingOptionView(option, true);
        selectedShippingLayout.addView(selectedView);
    }

    private void placeOrder() {
        if (selectedCartItems == null || selectedCartItems.isEmpty()) {
            Toast.makeText(getContext(), "Không có sản phẩm để đặt hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedAddress == null) {
            Toast.makeText(getContext(), "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedPaymentMethod == null) {
            Toast.makeText(getContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        OrderApi.PlaceOrderRequestBody requestBody = new OrderApi.PlaceOrderRequestBody();
        requestBody.items = selectedCartItems.stream().map(cartItem -> {
            OrderApi.CartItemInfo itemInfo = new OrderApi.CartItemInfo();
            itemInfo.productId = cartItem.productId;
            itemInfo.qty = cartItem.qty;
            return itemInfo;
        }).collect(Collectors.toCollection(ArrayList::new));

        requestBody.paymentMethod = this.selectedPaymentMethod.code;
        requestBody.shippingAddressId = this.selectedAddress.id;
        requestBody.shippingFee = this.shippingFee;
        requestBody.note = "Giao giờ hành chính, để bảo quản hộp cẩn thận";

        btnBuyNow.setEnabled(false);
        btnBuyNow.setText("ĐANG ĐẶT HÀNG...");

        RetrofitProvider.order().placeOrder(requestBody).enqueue(new Callback<OrderApi.PlaceOrderResponse>() {
            @Override
            public void onResponse(Call<OrderApi.PlaceOrderResponse> call, Response<OrderApi.PlaceOrderResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    // Lấy danh sách productId từ các sản phẩm đã đặt hàng thành công
                    List<String> productIdsToRemove = selectedCartItems.stream()
                            .map(item -> item.productId)
                            .collect(Collectors.toList());

                    // Xóa các sản phẩm đã đặt hàng khỏi giỏ hàng
                    CartRepository.getInstance().removeMultipleFromCart(productIdsToRemove, new CartRepository.CartCallback() {
                        @Override
                        public void onSuccess(List<CartApi.CartItem> items) {
                            Log.d("OrderPreview", "Successfully removed " + productIdsToRemove.size() + " items from cart");
                            if (isAdded()) {
                                getActivity().runOnUiThread(() -> showSuccessDialog());
                            }
                        }

                        @Override
                        public void onError(String error) {
                            Log.e("OrderPreview", "Order successful, but failed to remove items from cart: " + error);
                            // Ngay cả khi xóa lỗi, vẫn hiển thị dialog vì đặt hàng đã thành công
                            if (isAdded()) {
                                getActivity().runOnUiThread(() -> showSuccessDialog());
                            }
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Đặt hàng thất bại", Toast.LENGTH_SHORT).show();
                    btnBuyNow.setEnabled(true);
                    btnBuyNow.setText("MUA NGAY");
                }
            }

            @Override
            public void onFailure(Call<OrderApi.PlaceOrderResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnBuyNow.setEnabled(true);
                btnBuyNow.setText("MUA NGAY");
            }
        });
    }

    private void showSuccessDialog() {
        if (!isAdded() || getContext() == null) return;

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_place_order_success);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView btnClose = dialog.findViewById(R.id.btnCloseSuccess);
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            if (getView() != null)
                Navigation.findNavController(getView()).popBackStack();
        });

        dialog.setCancelable(false);
        dialog.show();
    }
}