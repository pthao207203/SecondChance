package com.example.secondchance.ui.shoporder;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentShopCanceledOrderDetailBinding;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.OrderDetailResponse;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CanceledShopOrderDetailFragment extends Fragment {

    private static final String TAG = "CanceledShopDetail";
    private FragmentShopCanceledOrderDetailBinding binding;
    private OrderApi orderApi;
    private String receivedShopOrderId;

    private ShopOrderProductDetailAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopCanceledOrderDetailBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            receivedShopOrderId = getArguments().getString("shopOrderId");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        orderApi = RetrofitProvider.order();

        setupRecyclerView();
        setupUI();

        if (receivedShopOrderId != null) {
            loadOrderDetail(receivedShopOrderId);
        } else {
            Log.e(TAG, "Order ID is null.");
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID đơn hàng", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void setupUI() {
        binding.btnContact.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng liên hệ đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadOrderDetail(String id) {
        orderApi.getOrderDetail(id).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> res) {
                if (!isAdded()) return;

                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Toast.makeText(requireContext(), "Không tải được dữ liệu chi tiết", Toast.LENGTH_SHORT).show();
                    return;
                }
                bindData(res.body().data);
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(OrderDetailResponse.Data data) {
        if (data.order != null) {

            if (binding.tvOrderId != null) binding.tvOrderId.setText(data.order.id != null ? data.order.id.toUpperCase() : "");

            binding.tvShippingFee.setText(formatVnd(data.order.orderShippingFee));
            binding.tvTotalAmount.setText(formatVnd(data.order.orderTotalAmount));

            if (data.order.orderShippingAddress != null) {
                var addr = data.order.orderShippingAddress;
                binding.tvReceiverName.setText(safe(addr.name));
                binding.tvReceiverPhone.setText(safe(addr.phone));

                String fullAddress = safe(addr.street) + ", " + safe(addr.ward) + ", " + safe(addr.province);
                binding.tvReceiverAddress.setText(fullAddress);
            }

            if (binding.tvPaymentMethod != null) {
                String methodCode = data.order.orderPaymentMethod;
                String methodText;

                if (methodCode == null) {
                    methodText = "Chưa xác định";
                } else {
                    switch (methodCode.toLowerCase()) {
                        case "cod":
                            methodText = "Thanh toán khi nhận hàng ";
                            break;
                        case "zalopay":
                            methodText = "Ví điện tử ZaloPay";
                            break;
                        case "wallet":
                            methodText = "Tiền trong ví";
                            break;
                        case "bank":
                            methodText = "Chuyển khoản ngân hàng";
                            break;
                        default:
                            methodText = "Thanh toán điện tử (" + methodCode + ")";
                            break;
                    }
                }
                binding.tvPaymentMethod.setText(methodText);
            }

        }

        productList.clear();
        if (data.order != null && data.order.orderItems != null) {
            for (OrderDetailResponse.OrderItem dto : data.order.orderItems) {
                productList.add(new ShopOrderProduct(
                        "ID",
                        dto.name,
                        "",
                        formatVnd(dto.price),
                        0,
                        dto.qty,
                        dto.imageUrl
                ));
            }
        }
        if (productAdapter != null) productAdapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        productAdapter = new ShopOrderProductDetailAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
    }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class ShopOrderProductDetailAdapter extends RecyclerView.Adapter<ShopOrderProductDetailAdapter.ProductViewHolder> {

        private final List<ShopOrderProduct> productList;
        private final Context context;

        ShopOrderProductDetailAdapter(Context context, List<ShopOrderProduct> productList) {
            this.context = context;
            this.productList = productList;
        }

        @NonNull
        @Override
        public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_canceled_order, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            holder.bind(productList.get(position));
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        static class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvSubtitle, tvPrice;

            ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitleDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            void bind(ShopOrderProduct product) {
                tvTitle.setText(product.getTitle());
                tvPrice.setText(product.getPrice());

                if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(product.getImageUrl())
                            .into(imgProduct);
                } else {
                    imgProduct.setImageResource(product.getImageRes());
                }

                if (tvSubtitle != null) {
                    tvSubtitle.setText(product.getSubtitle());
                }
            }
        }
    }
}
