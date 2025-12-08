package com.example.secondchance.ui.checkout;

import android.content.Intent;
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
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentCheckoutBinding;
import com.example.secondchance.dto.request.PaymentRequest;
import com.example.secondchance.dto.request.PreviewOrderRequest;
import com.example.secondchance.dto.response.PaymentResponse;
import com.example.secondchance.dto.response.PreviewOrderResponse;
import com.example.secondchance.util.LogApiError;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    
    // Danh sách item dùng để gửi lên BE (dù tới từ cart / product detail / thương lượng)
    private final List<CartApi.CartItem> checkoutItems = new ArrayList<>();
    
    private long serverShippingFee = 0;
    private long serverGrandTotal = 0;
    
    private enum PaymentMethod { COD, WALLET, ZALOPAY }
    private PaymentMethod currentPaymentMethod = PaymentMethod.COD;
    
    private CheckoutProductsAdapter productsAdapter;
    
    // 👉 Lưu list địa chỉ + địa chỉ đang chọn
    private List<PreviewOrderResponse.Address> addressList = new ArrayList<>();
    private PreviewOrderResponse.Address selectedAddress = null;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();
        updatePaymentUI();
        calculateDeliveryDate();
        
        // ---- LẤY ARG TỪ 3 NGUỒN: CART / DETAIL / NEGOTIATION ----
        handleArguments();
        
        // Gọi API preview đơn
        loadOrderPreview();
        
        binding.btnPaymentMethod.setOnClickListener(v -> showPaymentMethodDialog());
        binding.btnBuyNow.setOnClickListener(v -> handleBuyNow());
        
        // 👉 Mở dialog chọn địa chỉ
        binding.btnShippingAddress.setOnClickListener(v -> showAddressPickerDialog());
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
        binding.tvDeliveryDate.setText("Nhận vào khoảng " + sdf.format(calendar.getTime()));
    }
    
    private void handleArguments() {
        if (getArguments() == null) return;
        Bundle args = getArguments();
        
        // ---- CASE 1: TỪ GIỎ HÀNG (selectedItems có và không rỗng) ----
        if (args.containsKey("selectedItems")) {
            ArrayList<CartApi.CartItem> items =
              (ArrayList<CartApi.CartItem>) args.getSerializable("selectedItems");
            
            if (items != null && !items.isEmpty()) {
                checkoutItems.clear();
                checkoutItems.addAll(items);
                return; // đã lấy từ cart thì thôi, không cần check productId nữa
            }
        }
        
        // ---- CASE 2 + 3: MUA NGAY (TỪ DETAIL HOẶC TỪ THƯƠNG LƯỢNG) ----
        if (args.containsKey("productId")) {
            String pId = args.getString("productId");
            int qty = args.getInt("quantity", 1);
            
            if (pId != null && !pId.isEmpty()) {
                CartApi.CartItem item = new CartApi.CartItem();
                item.productId = pId;
                item.qty = qty <= 0 ? 1 : qty;
                
                checkoutItems.clear();
                checkoutItems.add(item);
            }
        }
    }
    
    private void loadOrderPreview() {
        if (checkoutItems.isEmpty()) {
            Toast.makeText(getContext(), "Không có sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        binding.tvDeliveryDate.setText("Đang tính toán...");
        binding.btnBuyNow.setEnabled(false);
        binding.btnBuyNow.setAlpha(0.5f);
        
        PreviewOrderRequest request = new PreviewOrderRequest();
        List<PreviewOrderRequest.Item> items = new ArrayList<>();
        
        for (CartApi.CartItem item : checkoutItems) {
            String pId = (item.productId != null)
              ? item.productId
              : (item.product != null ? item.product.id : null);
            
            if (pId != null) {
                items.add(new PreviewOrderRequest.Item(pId, item.qty));
            }
        }
        
        request.setItems(items);
        
        RetrofitProvider.order()
          .previewOrder(request)
          .enqueue(new Callback<PreviewOrderResponse>() {
              @Override
              public void onResponse(@NonNull Call<PreviewOrderResponse> call,
                                     @NonNull Response<PreviewOrderResponse> response) {
                  if (!isAdded()) return;
                  
                  if (response.isSuccessful() && response.body() != null && response.body().success) {
                      PreviewOrderResponse.Data data = response.body().getData();
                      
                      serverShippingFee = data.getShippingFee();
                      serverGrandTotal = data.getGrandTotal();
                      
                      DecimalFormat formatter = new DecimalFormat("#,###");
                      binding.tvTrasnportFee.setText(formatter.format(serverShippingFee));
                      binding.tvTotalPrice.setText(formatter.format(serverGrandTotal));
                      
                      binding.tvDeliveryDate.setText("Đã tính xong");
                      
                      if (productsAdapter != null) {
                          productsAdapter.setItems(data.getItems());
                      }
                      
                      // 👉 Lưu list địa chỉ
                      addressList = (data.getAddresses() != null) ? data.getAddresses() : new ArrayList<>();
                      
                      // 👉 Chọn địa chỉ ban đầu (default hoặc phần tử đầu)
                      if (!addressList.isEmpty()) {
                          PreviewOrderResponse.Address chosen = null;
                          for (PreviewOrderResponse.Address a : addressList) {
                              if (a != null && a.isDefault) {
                                  chosen = a;
                                  break;
                              }
                          }
                          if (chosen == null) chosen = addressList.get(0);
                          selectedAddress = chosen;
                      } else {
                          selectedAddress = null;
                      }
                      
                      // Cập nhật UI theo selectedAddress
                      updateShippingAddressUI();
                      
                      binding.btnBuyNow.setEnabled(true);
                      binding.btnBuyNow.setAlpha(1.0f);
                  } else {
                      Toast.makeText(getContext(),
                        "Lỗi: Không thể tính tiền đơn hàng",
                        Toast.LENGTH_SHORT).show();
                      binding.tvDeliveryDate.setText("Lỗi tính tiền");
                  }
              }
              
              @Override
              public void onFailure(@NonNull Call<PreviewOrderResponse> call,
                                    @NonNull Throwable t) {
                  if (!isAdded()) return;
                  Toast.makeText(getContext(), "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                  binding.tvDeliveryDate.setText("Lỗi mạng");
              }
          });
    }
    
    // 👉 Hiển thị địa chỉ (ưu tiên default)
    private void updateShippingAddressUI() {
        if (selectedAddress == null) {
            binding.tvReceiverName.setText("Chưa có địa chỉ");
            binding.tvReceiverPhone.setText("");
            binding.tvReceiverAddress.setText("Vui lòng thêm địa chỉ trong Hồ sơ > Địa chỉ");
            return;
        }
        
        String addr = buildAddressString(selectedAddress);
        binding.tvReceiverName.setText(selectedAddress.name != null ? selectedAddress.name : "");
        binding.tvReceiverPhone.setText(selectedAddress.phone != null ? selectedAddress.phone : "");
        binding.tvReceiverAddress.setText(addr);
    }
    
    private String buildAddressString(PreviewOrderResponse.Address a) {
        StringBuilder addr = new StringBuilder();
        if (a.street != null && !a.street.isEmpty()) addr.append(a.street);
        if (a.ward != null && !a.ward.isEmpty()) {
            if (addr.length() > 0) addr.append(", ");
            addr.append(a.ward);
        }
        if (a.province != null && !a.province.isEmpty()) {
            if (addr.length() > 0) addr.append(", ");
            addr.append(a.province);
        }
        if (a.country != null && !a.country.isEmpty()) {
            if (addr.length() > 0) addr.append(", ");
            addr.append(a.country);
        }
        return addr.toString();
    }
    
    // 👉 Dialog nhỏ chọn 1 địa chỉ
    private void showAddressPickerDialog() {
        if (addressList == null || addressList.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có địa chỉ để chọn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] displayItems = new String[addressList.size()];
        int checkedIndex = -1;
        
        for (int i = 0; i < addressList.size(); i++) {
            PreviewOrderResponse.Address a = addressList.get(i);
            String label = (a.label != null && !a.label.isEmpty()) ? (a.label + " - ") : "";
            String addr = buildAddressString(a);
            displayItems[i] = label + addr;
            
            if (selectedAddress != null && selectedAddress.id != null
              && selectedAddress.id.equals(a.id)) {
                checkedIndex = i;
            }
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
          .setTitle("Chọn địa chỉ nhận hàng")
          .setSingleChoiceItems(displayItems, checkedIndex, (dialog, which) -> {
              // ✅ Gán trực tiếp địa chỉ được chọn
              selectedAddress = addressList.get(which);
              updateShippingAddressUI();
              dialog.dismiss();
          })
          .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
          .show();
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
        
        layout.addView(createPaymentOptionView(dialog,
          "Thanh toán khi nhận hàng",
          R.drawable.ic_credit_card,
          PaymentMethod.COD));
        
        layout.addView(createPaymentOptionView(dialog,
          "Ví của tôi",
          R.drawable.ic_profile1,
          PaymentMethod.WALLET));
        
        layout.addView(createPaymentOptionView(dialog,
          "Thanh toán qua ZaloPay",
          R.drawable.ic_transport,
          PaymentMethod.ZALOPAY));
        
        dialog.setContentView(layout);
        dialog.show();
    }
    
    private View createPaymentOptionView(BottomSheetDialog dialog,
                                         String name,
                                         int iconRes,
                                         PaymentMethod method) {
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
        
        if (selectedAddress == null) {
            Toast.makeText(getContext(), "Vui lòng chọn địa chỉ nhận hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentPaymentMethod == PaymentMethod.ZALOPAY) {
            createZaloPayOrder();
        } else {
            Toast.makeText(requireContext(),
              "Phương thức này đang được phát triển...",
              Toast.LENGTH_SHORT).show();
        }
    }
    
    private void createZaloPayOrder() {
        binding.btnBuyNow.setEnabled(false);
        binding.btnBuyNow.setText("Đang tạo đơn...");
        
        List<PaymentRequest.Item> requestItems = new ArrayList<>();
        for (CartApi.CartItem item : checkoutItems) {
            String pId = (item.productId != null)
              ? item.productId
              : (item.product != null ? item.product.id : null);
            if (pId != null) {
                requestItems.add(new PaymentRequest.Item(pId, item.qty));
            }
        }
        
        // 👉 Lấy info từ selectedAddress thay vì hardcode
        PaymentRequest.ShippingAddress address = new PaymentRequest.ShippingAddress();
        if (selectedAddress != null) {
            address.setFullName(selectedAddress.name);
            address.setPhone(selectedAddress.phone);
            address.setAddress(buildAddressString(selectedAddress));
        } else {
            address.setFullName("Khách hàng");
            address.setPhone("0909123456");
            address.setAddress("TP.HCM");
        }
        
        PaymentRequest request = new PaymentRequest();
        request.setItems(requestItems);
        request.setShippingAddress(address);
        request.setShippingFee(serverShippingFee);
        
        RetrofitProvider.payment()
          .createZaloPayUrl(request)
          .enqueue(new Callback<PaymentResponse>() {
              @Override
              public void onResponse(@NonNull Call<PaymentResponse> call,
                                     @NonNull Response<PaymentResponse> response) {
                  binding.btnBuyNow.setEnabled(true);
                  binding.btnBuyNow.setText("MUA NGAY");
                  
                  if (response.isSuccessful() && response.body() != null) {
                      String payUrl = response.body().getPayUrl();
                      if (payUrl != null && !payUrl.isEmpty()) {
                          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(payUrl));
                          startActivity(intent);
                      } else {
                          Toast.makeText(getContext(),
                            "Lỗi: Link thanh toán rỗng",
                            Toast.LENGTH_SHORT).show();
                      }
                  } else {
                      Toast.makeText(getContext(),
                        "Tạo đơn thất bại: " + response.code(),
                        Toast.LENGTH_SHORT).show();
                      LogApiError.log("CheckoutFragment", response.message(), response);
                  }
              }
              
              @Override
              public void onFailure(@NonNull Call<PaymentResponse> call,
                                    @NonNull Throwable t) {
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
