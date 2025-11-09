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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation; // <-- Thêm
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // <-- Thêm

import com.example.secondchance.R;
// === MODEL ĐỒNG BỘ ===
import com.example.secondchance.data.model.ShopOrderProduct;
//
// === THAY ĐỔI BINDING ===
import com.example.secondchance.databinding.FragmentShopSoldOrderDetailBinding;
//
// === XÓA ADAPTER CŨ ===
// import com.example.secondchance.ui.shoporder.adapter.ShopOrderProductAdapter;
import com.example.secondchance.viewmodel.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

public class SoldShopOrderDetailFragment extends Fragment {
    // Xóa implements ShopOrderProductAdapter.OnSelectionChangedListener

    private static final String TAG = "SoldShopOrderDetail";

    // === BINDING ĐÃ SỬA ===
    private FragmentShopSoldOrderDetailBinding binding;

    private SharedViewModel sharedViewModel;
    private String receivedShopOrderId;

    // === LOGIC MỚI: Nhận trạng thái đánh giá ===
    private boolean isEvaluated = false;

    // === ADAPTER VÀ LIST MỚI ===
    private ShopOrderProductDetailAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // === SỬA BINDING ===
        binding = FragmentShopSoldOrderDetailBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // === NHẬN ARGUMENTS (BAO GỒM isEvaluated) ===
        if (getArguments() != null) {
            receivedShopOrderId = getArguments().getString("shopOrderId");
            isEvaluated = getArguments().getBoolean("isEvaluated", false); // Nhận
            Log.d(TAG, "Hiển thị chi tiết đơn ĐÃ BÁN: " + receivedShopOrderId + " / Đã đánh giá: " + isEvaluated);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // sharedViewModel.updateTitle("Chi tiết đơn hàng đã bán"); // (Tùy chọn)

        setupProductRecyclerView();
        loadDummyProductData(receivedShopOrderId); // Tải data
        productAdapter.notifyDataSetChanged();

        // === LOGIC NÚT (THEO YÊU CẦU) ===
        if (isEvaluated) {
            binding.btnShopReply.setVisibility(View.VISIBLE);
        } else {
            binding.btnShopReply.setVisibility(View.GONE);
        }

        binding.btnShopReply.setOnClickListener(v -> {
            // TODO: Mở dialog/fragment để shop trả lời đánh giá
            Toast.makeText(getContext(), "Mở màn hình trả lời đánh giá...", Toast.LENGTH_SHORT).show();
        });

        // Cập nhật thông tin đơn hàng (ví dụ)
        binding.tvOrderId.setText(receivedShopOrderId);
        binding.tvReceiverName.setText("Cá Biết Bay");
        binding.tvReceiverPhone.setText("0333 333 xxx");
        binding.tvShippingFee.setText("50.000"); // (Lấy từ API)
        binding.tvTotalAmount.setText("200.000"); // (Lấy từ API)
    }

    private void setupProductRecyclerView() {
        productAdapter = new ShopOrderProductDetailAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
    }

    // Xóa hàm onSelectionChanged()

    // === SỬA: Tải ShopOrderProduct (khớp với SoldShopFragment) ===
    private void loadDummyProductData(String shopOrderId) {
        productList.clear();

        if ("SHOP001".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct(
                    "P-101", "Giỏ gỗ cắm hoa", "Đã giao", "50.000",
                    R.drawable.sample_flower, 1
            ));
        } else if ("SHOP002".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct(
                    "P-102", "Tranh sơn mài", "Đã giao", "250.000",
                    R.drawable.sample_flower, 1
            ));
        } else if ("SHOP003".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct(
                    "P-103", "Bình gốm cổ", "Đã giao", "150.000",
                    R.drawable.sample_flower, 1
            ));
        } else {
            productList.add(new ShopOrderProduct("P-ERR", "Lỗi tải sản phẩm", "", "0", R.drawable.sample_flower, 0));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // =================================================================
    // === ADAPTER MỚI ĐỂ HIỂN THỊ ShopOrderProduct ===
    // =================================================================
    // (Adapter này giống hệt adapter chúng ta cần ở các file chi tiết khác)
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