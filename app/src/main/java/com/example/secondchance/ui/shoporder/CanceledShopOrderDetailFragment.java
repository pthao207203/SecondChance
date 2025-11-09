package com.example.secondchance.ui.shoporder;

import android.content.Context; // <-- Thêm
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // <-- Thêm
import android.widget.TextView; // <-- Thêm
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // <-- Thêm

// === THAY ĐỔI BINDING ===
import com.example.secondchance.databinding.FragmentShopCanceledOrderDetailBinding;
//
import com.example.secondchance.R;
// === MODEL ĐỒNG BỘ ===
import com.example.secondchance.data.model.ShopOrderProduct;
//
// XÓA MODEL CŨ
// import com.example.secondchance.data.model.ShopOrderItem;
// import com.example.secondchance.ui.shoporder.adapter.ShopOrderItemAdapter;

import java.util.ArrayList;
import java.util.List;


public class CanceledShopOrderDetailFragment extends Fragment {
    private static final String TAG = "CanceledShopOrderDetailFrag";

    // === BINDING ĐÃ SỬA ===
    private FragmentShopCanceledOrderDetailBinding binding;

    private String receivedOrderId;

    // === ADAPTER VÀ LIST MỚI ===
    private ShopOrderProductDetailAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // === SỬA BINDING ===
        binding = FragmentShopCanceledOrderDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            Log.d(TAG, "Received Shop Order ID: " + receivedOrderId);
        } else {
            Log.w(TAG, "Arguments are null!");
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupProductRecyclerView(); // Dùng adapter mới

        if (receivedOrderId != null) {
            loadCanceledOrderDetails(receivedOrderId); // Dùng data mới
        } else {
            Log.e(TAG, "Order ID is null.");
            Toast.makeText(getContext(), "Lỗi tải chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
        }

        // Sửa logic nút (từ "Mua lại" thành "Liên hệ" như trong XML)
        binding.btnContact.setOnClickListener(v -> {
            Log.d(TAG, "Contact clicked for order: " + receivedOrderId);
            Toast.makeText(getContext(), "Mở màn hình liên hệ...", Toast.LENGTH_SHORT).show();
            // TODO: logic gọi điện hoặc nhắn tin
        });

    }

    // === SỬA: Dùng Adapter mới ===
    private void setupProductRecyclerView() {
        productAdapter = new ShopOrderProductDetailAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        Log.d(TAG, "Product RecyclerView setup complete.");
    }

    // === SỬA: Tải data ShopOrderProduct ===
    private void loadCanceledOrderDetails(String orderId) {
        Log.d(TAG, "Load canceled order details for " + orderId);

        loadDummyProductData(orderId); // Tải data khớp
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product list updated");
        }
        // TODO: Cập nhật các TextView khác (Tổng tiền, Người nhận...)
    }

    // === SỬA: Tải data ShopOrderProduct (khớp với CancelShopFragment) ===
    private void loadDummyProductData(String shopOrderId) {
        productList.clear();

        if ("CANCELED001".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct(
                    "P-201", "Giỏ gỗ cắm hoa", "Giá cố định",
                    "50.000", R.drawable.sample_flower, 1
            ));
        } else {
            // Dự phòng
            productList.add(new ShopOrderProduct("P-ERR", "Lỗi tải sản phẩm", "", "0", R.drawable.sample_flower, 0));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG, "onDestroyView called");
    }

    // =================================================================
    // === ADAPTER MỚI ĐỂ HIỂN THỊ ShopOrderProduct ===
    // =================================================================
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
            // **LƯU Ý:** Bạn cần tạo một file layout cho item này
            // Ví dụ: R.layout.item_shop_order_product_detail
            // Tôi sẽ tạm dùng layout cũ 'item_canceled_order' vì nó có các ID gần giống
            View view = LayoutInflater.from(context).inflate(R.layout.item_canceled_order, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
            ShopOrderProduct product = productList.get(position);
            holder.bind(product);
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
                // TODO: Ánh xạ ID từ layout 'item_shop_order_product_detail.xml'
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitleDate); // Tạm dùng ID này
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            void bind(ShopOrderProduct product) {
                tvTitle.setText(product.getTitle());
                tvPrice.setText(product.getPrice());
                imgProduct.setImageResource(product.getImageRes());
                if (tvSubtitle != null) {
                    tvSubtitle.setText(product.getSubtitle());
                }
            }
        }
    }
}