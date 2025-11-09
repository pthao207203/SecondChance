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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding; // <-- Import ViewBinding

import com.example.secondchance.R;
// === MODEL ĐỒNG BỘ ===
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
//
// === IMPORT TẤT CẢ BINDING CẦN THIẾT ===
import com.example.secondchance.databinding.FragmentShopRefundConfirmedDetailBinding;
import com.example.secondchance.databinding.FragmentShopRefundDeliveringDetailBinding;
import com.example.secondchance.databinding.FragmentShopRefundNotConfirmedDetailBinding;
import com.example.secondchance.databinding.FragmentShopRefundSuccessfulDetailBinding;
//
import com.example.secondchance.viewmodel.SharedViewModel;
import java.util.ArrayList;
import java.util.List;

public class RefundShopOrderDetailFragment extends Fragment {
    private static final String TAG = "RefundShopOrderDetail";

    // Một biến ViewBinding chung
    private ViewBinding binding;

    private SharedViewModel sharedViewModel;
    private String receivedOrderId;
    private ShopOrder.RefundStatus receivedRefundStatus;

    // === ADAPTER VÀ LIST SẢN PHẨM ===
    private ShopOrderProductDetailAdapter productAdapter;
    private final List<ShopOrderProduct> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Lấy arguments TRƯỚC để quyết định layout
        if (getArguments() != null) {
            receivedOrderId = getArguments().getString("orderId");
            try {
                receivedRefundStatus = (ShopOrder.RefundStatus) getArguments().getSerializable("refundStatus");
            } catch (Exception e) {
                Log.e(TAG, "Error getting refundStatus from arguments", e);
                receivedRefundStatus = ShopOrder.RefundStatus.NOT_CONFIRMED; // Mặc định
            }
        } else {
            Log.e(TAG, "Arguments are null!");
            Toast.makeText(getContext(), "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
            return null;
        }

        // === LOGIC CHỌN LAYOUT DỰA TRÊN STATUS (ĐÃ KHỚP VỚI ADAPTER) ===
        // (Khớp với logic Adapter của RefundShopFragment)
        switch (receivedRefundStatus) {
            case CONFIRMED:
                // Adapter dùng: item_shop_refund_confirmed
                // Layout chi tiết: fragment_shop_refund_confirmed_detail
                FragmentShopRefundConfirmedDetailBinding confirmedBinding = FragmentShopRefundConfirmedDetailBinding.inflate(inflater, container, false);
                binding = confirmedBinding;
                break;
            case DELIVERING:
                // Adapter dùng: item_shop_refund_delivering
                // Layout chi tiết: fragment_shop_refund_delivering_detail
                FragmentShopRefundDeliveringDetailBinding deliveringBinding = FragmentShopRefundDeliveringDetailBinding.inflate(inflater, container, false);
                binding = deliveringBinding;
                break;
            case SUCCESSFUL:
                // Adapter dùng: item_shop_refund_successful
                // Layout chi tiết: fragment_shop_refund_successful_detail
                FragmentShopRefundSuccessfulDetailBinding successfulBinding = FragmentShopRefundSuccessfulDetailBinding.inflate(inflater, container, false);
                binding = successfulBinding;
                break;
            case NOT_CONFIRMED:
            default:
                // Adapter dùng: item_shop_refund_not_confirmed
                // Layout chi tiết: fragment_shop_refund_not_confirmed_detail
                FragmentShopRefundNotConfirmedDetailBinding notConfirmedBinding = FragmentShopRefundNotConfirmedDetailBinding.inflate(inflater, container, false);
                binding = notConfirmedBinding;
                break;
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // 1. Tải dữ liệu sản phẩm
        loadDummyProductData(receivedOrderId);

        // 2. Gán listener cho các nút (Đã sửa logic)
        setupButtonListeners();

        // 3. Cài đặt hiển thị sản phẩm (Đã sửa logic)
        // Chỉ layout DELIVERING có RecyclerView (rvOrderItems)
        // 3 layout còn lại (NOT_CONFIRMED, CONFIRMED, SUCCESSFUL) dùng View tĩnh
        if (receivedRefundStatus == ShopOrder.RefundStatus.DELIVERING) {
            RecyclerView rvOrderItems = view.findViewById(R.id.rvOrderItems);
            if (rvOrderItems != null) {
                setupProductRecyclerView(rvOrderItems);
                if(productAdapter != null) productAdapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Lỗi: layout delivering không có rvOrderItems!");
            }
        } else {
            // 3 trạng thái còn lại dùng View tĩnh, gán dữ liệu
            if (!productList.isEmpty()) {
                bindStaticProductData(view, productList.get(0));
            }
        }
    }

    // === HÀM MỚI: Gán dữ liệu cho 3 layout dùng View tĩnh ===
    private void bindStaticProductData(View view, ShopOrderProduct product) {
        try {
            ImageView imgProduct = view.findViewById(R.id.imgProduct);
            TextView tvTitle = view.findViewById(R.id.tvTitle);
            TextView tvPrice = view.findViewById(R.id.tvPrice);
            TextView tvQuantity = view.findViewById(R.id.tvQuantity);
            TextView tvDescription = view.findViewById(R.id.tvDescription);

            if(imgProduct != null) imgProduct.setImageResource(product.getImageRes());
            if(tvTitle != null) tvTitle.setText(product.getTitle());
            if(tvPrice != null) tvPrice.setText(product.getPrice());
            if(tvQuantity != null) tvQuantity.setText("Số lượng: " + product.getQuantity());
            if(tvDescription != null) tvDescription.setText(product.getSubtitle());

        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi gán dữ liệu tĩnh cho sản phẩm", e);
        }
    }

    // === HÀM ĐÃ SỬA LỖI LOGIC (KHÔNG BỊ HOÁN ĐỔI) ===
    private void setupButtonListeners() {
        switch (receivedRefundStatus) {

            // LOGIC CHO SHOP (chờ khách gửi hàng)
            case CONFIRMED:
                // Layout: fragment_shop_refund_confirmed_detail (Shop đã chấp nhận)
                // Nút: btnAccept (Shop đồng ý), btnResendRequest (Shop khiếu nại)
                ((FragmentShopRefundConfirmedDetailBinding) binding).btnAccept.setOnClickListener(v -> {
                    Log.d(TAG, "Shop 'Đồng ý' với yêu cầu (chờ khách gửi)");
                    navigateBack();
                });
                ((FragmentShopRefundConfirmedDetailBinding) binding).btnResendRequest.setOnClickListener(v -> {
                    Log.d(TAG, "Shop 'Khiếu nại' (chờ khách gửi)");
                    Toast.makeText(getContext(), "Đang xử lý khiếu nại...", Toast.LENGTH_SHORT).show();
                });
                break;

            // LOGIC CHO SHOP (Shop chờ nhận hàng về)
            case DELIVERING:
                // Layout: fragment_shop_refund_delivering_detail
                ((FragmentShopRefundDeliveringDetailBinding) binding).layoutStatusNotConfirmed.setVisibility(View.VISIBLE);
                // Nút: btnReceiveOrder (Shop xác nhận đã nhận hàng)
                ((FragmentShopRefundDeliveringDetailBinding) binding).btnReceiveOrder.setOnClickListener(v -> {
                    Log.d(TAG, "Shop XÁC NHẬN ĐÃ NHẬN LẠI HÀNG");
                    // TODO: Gọi API cập nhật trạng thái -> SUCCESSFUL
                    Toast.makeText(getContext(), "Đã xác nhận nhận lại hàng", Toast.LENGTH_SHORT).show();
                    navigateBack();
                });
                break;

            // LOGIC CHO SHOP (đã xong)
            case SUCCESSFUL:
                // Layout: fragment_shop_refund_successful_detail
                // Không có nút
                break;

            // LOGIC CHO SHOP (Shop cần duyệt)
            case NOT_CONFIRMED:
            default:
                // Layout: fragment_shop_refund_not_confirmed_detail
                // Nút: btnSendRequest (Shop HỦY yêu cầu)
                // LƯU Ý: Layout của bạn đang có nút "Hủy yêu cầu" (btnSendRequest)
                // Lẽ ra nó phải là "Chấp nhận" và "Từ chối"
                ((FragmentShopRefundNotConfirmedDetailBinding) binding).btnSendRequest.setOnClickListener(v -> {
                    Log.d(TAG, "Shop 'HỦY YÊU CẦU' hoàn trả của khách");
                    // TODO: Gọi API hủy
                    Toast.makeText(getContext(), "Đã hủy yêu cầu hoàn trả", Toast.LENGTH_SHORT).show();
                    navigateBack();
                });
                break;
        }
    }

    // Hàm quay lại và refresh
    private void navigateBack() {
        sharedViewModel.refreshOrderLists();
        sharedViewModel.requestTabChange(4); // Chuyển về tab Refund
        NavController navController = NavHostFragment.findNavController(this);
        navController.popBackStack();
    }

    // Cài đặt RecyclerView (chỉ dùng cho 1 trạng thái)
    private void setupProductRecyclerView(RecyclerView rv) {
        productAdapter = new ShopOrderProductDetailAdapter(getContext(), productList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(productAdapter);
        rv.setNestedScrollingEnabled(false);
    }

    // Tải data sản phẩm (khớp với RefundShopFragment)
    private void loadDummyProductData(String shopOrderId) {
        productList.clear();

        if ("REFUND001".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct("P-401", "Giỏ gỗ cắm hoa", "Sản phẩm lỗi", "50.000", R.drawable.sample_flower, 1));
        } else if ("REFUND002".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct("P-402", "Tranh sơn mài", "Khác mô tả", "250.000", R.drawable.sample_flower, 1));
        } else if ("REFUND003".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct("P-403", "Bình gốm cổ", "Bị vỡ", "150.000", R.drawable.sample_flower, 1));
        } else if ("REFUND004".equals(shopOrderId)) {
            productList.add(new ShopOrderProduct("P-404", "Nhẫn kim cương", "Giao sai mẫu", "500.000", R.drawable.sample_flower, 1));
        } else {
            productList.add(new ShopOrderProduct("P-ERR", "Lỗi tải sản phẩm", "", "0", R.drawable.sample_flower, 0));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Hủy binding chung
    }

    // =================================================================
    // === ADAPTER NÀY CHỈ DÙNG CHO TRẠNG THÁI 'DELIVERING' ===
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
            // **LƯU Ý:**
            // Bạn cần tạo 1 file layout item (ví dụ: item_shop_order_product_detail.xml)
            // TẠM DÙNG R.layout.item_canceled_order
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
                imgProduct = itemView.findViewById(R.id.imgProduct);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvSubtitle = itemView.findViewById(R.id.tvSubtitleDate);
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