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

import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.R;
// MODEL MỚI ĐANG SỬ DỤNG
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
//
import com.example.secondchance.databinding.FragmentShopConfirmOrderDetailBinding;
// XÓA ADAPTER CŨ
// import com.example.secondchance.ui.order.adapter.OrderItemAdapter;
import java.util.ArrayList;
import java.util.List;

public class ConfirmShopOrderDetailFragment extends Fragment {

    private static final String TAG = "ConfirmShopOrderDetailFrag";

    // Đã khớp với XML mới
    private FragmentShopConfirmOrderDetailBinding binding;

    private String receivedOrderId;
    private ShopOrder.ShopOrderType receivedOrderType;

    // === THAY ĐỔI: Dùng Adapter và Model mới ===
    private ShopOrderProductAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();
    // === KẾT THÚC THAY ĐỔI ===

    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopConfirmOrderDetailBinding.inflate(inflater, container, false);

        // Lấy Arugments (Đã đúng)
        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            try {
                receivedOrderType = (ShopOrder.ShopOrderType) getArguments().getSerializable("orderType");
                Log.d(TAG, "Received ShopOrder ID: " + receivedOrderId + ", Type: " + receivedOrderType);
            } catch (Exception e) {
                Log.e(TAG, "Error getting orderType from arguments", e);
                receivedOrderType = null;
            }
        } else {
            Log.w(TAG, "Arguments are null!");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        setupRecyclerView(); // Cài đặt adapter mới

        if (receivedOrderId != null) {
            loadOrderDetails(receivedOrderId); // Tải dữ liệu ShopOrderProduct
        } else {
            Log.e(TAG, "ShopOrder ID is null, cannot load details.");
            Toast.makeText(getContext(), "Không thể tải chi tiết đơn hàng cửa hàng.", Toast.LENGTH_SHORT).show();
        }

        setupButtonListeners(); // Cài đặt nút (Đã đúng)
    }

    // === THAY ĐỔI: Tải dữ liệu ShopOrderProduct ===
    private void loadOrderDetails(String orderId) {
        Log.d(TAG, "Loading ShopOrderProduct list for shop order " + orderId);
        productList.clear();

        // Giả lập tải dữ liệu khớp với ID từ ConfirmationShopFragment
        // (Trong ứng dụng thật, bạn sẽ lấy từ ViewModel)
        if ("S-ORD-001".equals(orderId)) {
            productList.add(new ShopOrderProduct(
                    "P102", "Giỏ gỗ cắm hoa", "Loại nhỏ, đan tre",
                    "50.000", R.drawable.sample_flower, 1
            ));
            // Nếu đơn hàng có nhiều SP, bạn thêm vào đây
            // productList.add(new ShopOrderProduct(...));
        } else {
            // Trường hợp dự phòng
            Log.w(TAG, "Order ID không khớp, tải dữ liệu mặc định");
            productList.add(new ShopOrderProduct("P-ERR", "Lỗi tải sản phẩm", "", "0", R.drawable.sample_flower, 0));
        }


        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product list updated for RecyclerView");
        }
    }

    // === THAY ĐỔI: Dùng Adapter mới ===
    private void setupRecyclerView() {
        productAdapter = new ShopOrderProductAdapter(getContext(), productList);
        binding.rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvOrderItems.setAdapter(productAdapter);
        binding.rvOrderItems.setNestedScrollingEnabled(false);
        Log.d(TAG, "RecyclerView setup complete with ShopOrderProductAdapter.");
    }

    // Hàm này đã khớp với XML mới (btnConfirmOrder)
    private void setupButtonListeners() {
        if (receivedOrderType == ShopOrder.ShopOrderType.UNCONFIRMED) {
            binding.btnConfirmOrder.setVisibility(View.VISIBLE);
            binding.btnConfirmOrder.setOnClickListener(v -> {
                Log.d(TAG, "Shop đã XÁC NHẬN đơn hàng: " + receivedOrderId);
                Toast.makeText(getContext(), "Đã xác nhận đơn hàng!", Toast.LENGTH_SHORT).show();

                // TODO: Gọi ViewModel để thực hiện API XÁC NHẬN đơn hàng

                // Sau khi xác nhận, refresh list và chuyển sang tab "Đang giao" (index 1)
                sharedViewModel.refreshOrderLists();
                sharedViewModel.requestTabChange(1);

                NavController navController = Navigation.findNavController(requireView());
                navController.popBackStack();
            });
        } else {
            binding.btnConfirmOrder.setVisibility(View.GONE);
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
    // (Bạn sẽ cần tạo file layout 'item_shop_order_product_detail.xml' cho adapter này)
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
            // **LƯU Ý QUAN TRỌNG:**
            // Bạn cần tạo một file layout XML mới, ví dụ: 'R.layout.item_shop_order_product_detail'
            // File này chỉ chứa các View để hiển thị 1 sản phẩm (ảnh, tên, giá, số lượng)
            // Tôi sẽ tạm dùng 'R.layout.item_canceled_order' vì nó có các ID gần giống
            // TỐT NHẤT là bạn nên tạo layout riêng.
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
            TextView tvTitle, tvSubtitle, tvPrice, tvQuantity; // Giả sử layout có các ID này

            ProductViewHolder(@NonNull View itemView) {
                super(itemView);
                // Ánh xạ các view từ item layout của bạn
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitleDate); // Tạm dùng ID này
                tvPrice = itemView.findViewById(R.id.tvPrice);
                // tvQuantity = itemView.findViewById(R.id.tvQuantity); // Layout 'item_canceled_order' không có
            }

            void bind(ShopOrderProduct product) {
                tvTitle.setText(product.getTitle());
                tvPrice.setText(product.getPrice());
                imgProduct.setImageResource(product.getImageRes());

                // (Tùy chọn) Gán subtitle (mô tả) và số lượng
                if (tvSubtitle != null) {
                    tvSubtitle.setText(product.getSubtitle());
                }
                // if (tvQuantity != null) {
                //    tvQuantity.setText("x" + product.getQuantity());
                // }
            }
        }
    }
}