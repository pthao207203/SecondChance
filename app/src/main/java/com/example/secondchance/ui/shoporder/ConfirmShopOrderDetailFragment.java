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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.R;
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.databinding.FragmentShopConfirmOrderDetailBinding;

import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.OrderDetailResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmShopOrderDetailFragment extends Fragment {

    private static final String TAG = "ConfirmShopDetailFrag";
    private FragmentShopConfirmOrderDetailBinding binding;
    private String receivedOrderId;
    private ShopOrder.ShopOrderType receivedOrderType;

    private ShopOrderProductAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();

    private SharedViewModel sharedViewModel;
    private OrderApi orderApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopConfirmOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("shopOrderId");
            try {
                receivedOrderType = (ShopOrder.ShopOrderType) getArguments().getSerializable("orderType");
            } catch (Exception e) {
                receivedOrderType = ShopOrder.ShopOrderType.UNCONFIRMED;
            }
        } else {
            Toast.makeText(getContext(), "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();

        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        orderApi = RetrofitProvider.order();

        setupRecyclerView();

        if (receivedOrderId != null) {
            loadOrderDetails(receivedOrderId);
        }

        setupButtonListeners();
    }

    private void loadOrderDetails(String orderId) {
        orderApi.getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> res) {
                if (!isAdded()) return;
                if (res.isSuccessful() && res.body() != null && res.body().data != null) {
                    bindData(res.body().data);
                } else {
                    Toast.makeText(getContext(), "Không tải được chi tiết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(OrderDetailResponse.Data data) {
        if (data.order != null) {

            if (binding.tvShippingFee != null) binding.tvShippingFee.setText(formatVnd(data.order.orderShippingFee));
            if (binding.tvTotalAmount != null) binding.tvTotalAmount.setText(formatVnd(data.order.orderTotalAmount));

            if (data.order.orderShippingAddress != null) {
                var addr = data.order.orderShippingAddress;
                if (binding.tvReceiverName != null) binding.tvReceiverName.setText(safe(addr.name));
                if (binding.tvReceiverPhone != null) binding.tvReceiverPhone.setText(safe(addr.phone));

                String address = safe(addr.street) + ", " + safe(addr.ward) + ", " + safe(addr.province);
                if (binding.tvReceiverAddress != null) binding.tvReceiverAddress.setText(address);
            }

            if (binding.tvPaymentMethod != null) {
                String method = "cod".equalsIgnoreCase(data.order.orderPaymentMethod) ? "Tiền mặt" : "Ví điện tử (ZaloPay)";
                binding.tvPaymentMethod.setText(method);
            }
        }

        productList.clear();
        if (data.order != null && data.order.orderItems != null) {
            for (OrderDetailResponse.OrderItem dto : data.order.orderItems) {
                productList.add(new ShopOrderProduct(
                        "ID", dto.name, "", formatVnd(dto.price), 0, dto.qty, dto.imageUrl
                ));
            }
        }
        if (productAdapter != null) productAdapter.notifyDataSetChanged();
    }

    private void setupButtonListeners() {

        binding.btnConfirmOrder.setOnClickListener(v -> {
            if (receivedOrderId != null) {
                confirmOrder(receivedOrderId);
            }
        });
    }

    private void confirmOrder(String orderId) {

        binding.btnConfirmOrder.setEnabled(false);
        binding.btnConfirmOrder.setText("Đang xử lý...");

        orderApi.confirmOrder(orderId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> response) {
                if (!isAdded()) return;

                binding.btnConfirmOrder.setEnabled(true);
                binding.btnConfirmOrder.setText("XÁC NHẬN");

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(getContext(), "Đã xác nhận đơn hàng!", Toast.LENGTH_SHORT).show();

                    sharedViewModel.refreshOrderLists();

                    sharedViewModel.requestTabChange(1);

                    NavController navController = Navigation.findNavController(requireView());
                    navController.popBackStack();

                } else {
                    String msg = "Lỗi xác nhận. Mã lỗi: " + response.code();
                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                binding.btnConfirmOrder.setEnabled(true);
                binding.btnConfirmOrder.setText("XÁC NHẬN");
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ShopOrderProductAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
    }

    private String formatVnd(long amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
    }

    private String safe(String s) { return s == null ? "" : s; }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class ShopOrderProductAdapter extends RecyclerView.Adapter<ShopOrderProductAdapter.ProductViewHolder> {
        private final List<ShopOrderProduct> productList;
        private final Context context;

        ShopOrderProductAdapter(Context context, List<ShopOrderProduct> productList) {
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
        public int getItemCount() { return productList.size(); }

        static class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct; TextView tvTitle, tvSubtitle, tvPrice;
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
                if (product.getImageUrl() != null) {
                    Glide.with(itemView.getContext()).load(product.getImageUrl()).into(imgProduct);
                } else {
                    imgProduct.setImageResource(R.drawable.sample_flower);
                }
                if (tvSubtitle != null) tvSubtitle.setText(product.getSubtitle());
            }
        }
    }
}
