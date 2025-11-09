package com.example.secondchance.ui.shoporder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // <-- Thêm import này
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.databinding.FragmentShopDeliveringBinding;
import java.util.ArrayList;
import java.util.List;

public class DeliveringShopFragment extends Fragment {

    private FragmentShopDeliveringBinding binding;
    private DeliveringShopAdapter adapter;
    private final List<ShopOrder> shopOrderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShopDeliveringBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDummyData(); // Sẽ tải 2 loại đơn hàng

        adapter = new DeliveringShopAdapter(shopOrderList, (orderId, deliveryStatus) -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("shopOrderId", orderId);
                if (deliveryStatus != null) {
                    args.putSerializable("deliveryStatus", deliveryStatus);
                }
                nav.navigate(R.id.action_shopOrderFragment_to_deliveringShopOrderDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể mở chi tiết đơn giao của shop.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvDeliveringOrders.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvDeliveringOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    // ===================================
    // === HÀM loatDummyData ĐÃ CẬP NHẬT ===
    // === (Thêm 2 loại đơn hàng)      ===
    // ===================================
    private void loadDummyData() {
        shopOrderList.clear();

        // --- ĐƠN 1: ĐANG GIAO (Sử dụng layout item_shop_delivering_order) ---
        List<ShopOrderProduct> items1 = new ArrayList<>();
        items1.add(new ShopOrderProduct("P-301", "Đơn giao nhẫn kim cương", "Giá cố định", "50.000", R.drawable.ic_ring, 1));
        shopOrderList.add(new ShopOrder(
                "SHOPDELIV001", // id
                items1, // List<ShopOrderProduct>
                "50.000", // totalPrice
                "18/06/2025", // date
                "Đã đến bưu cục Trần Thị ABC", // statusText
                "Đang trên đường giao", // description
                null, // type
                false, // isEvaluated
                null, // refundStatus
                ShopOrder.DeliveryOverallStatus.AT_POST_OFFICE // <--- TRẠNG THÁI 1
        ));

        // --- ĐƠN 2: CHƯA GIAO (Sử dụng layout item_shop_not_yet_delivering_order) ---
        List<ShopOrderProduct> items2 = new ArrayList<>();
        items2.add(new ShopOrderProduct("P-302", "Bình gốm sứ", "Hàng dễ vỡ", "150.000", R.drawable.sample_flower, 1));
        shopOrderList.add(new ShopOrder(
                "SHOPDELIV002", // id
                items2, // List<ShopOrderProduct>
                "150.000", // totalPrice
                "19/06/2025", // date
                "Bạn chưa giao đơn hàng cho bưu cục", // statusText
                "Đã xác nhận, đang đóng gói", // description
                null, // type
                false, // isEvaluated
                null, // refundStatus
                ShopOrder.DeliveryOverallStatus.PACKAGED // <--- TRẠNG THÁI 2
        ));


        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private interface OnShopOrderClick {
        void openDetail(String shopOrderId, @Nullable ShopOrder.DeliveryOverallStatus deliveryStatus);
    }

    // ===================================
    // === ADAPTER ĐÃ NÂNG CẤP ĐỂ HỖ TRỢ 2 VIEW TYPE ===
    // ===================================
    private static class DeliveringShopAdapter extends RecyclerView.Adapter<DeliveringShopAdapter.ShopOrderViewHolder> {

        private final List<ShopOrder> items;
        private final OnShopOrderClick listener;

        // Định nghĩa 2 loại view
        private static final int VIEW_TYPE_NOT_YET_DELIVERING = 1;
        private static final int VIEW_TYPE_DELIVERING = 2;

        DeliveringShopAdapter(List<ShopOrder> items, OnShopOrderClick listener) {
            this.items = items;
            this.listener = listener;
        }

        @Override
        public int getItemViewType(int position) {
            ShopOrder order = items.get(position);
            // Dựa vào trạng thái đơn hàng để quyết định dùng layout nào
            if (order.getDeliveryStatus() == ShopOrder.DeliveryOverallStatus.PACKAGED) {
                return VIEW_TYPE_NOT_YET_DELIVERING;
            } else {
                // Các trạng thái AT_POST_OFFICE, DELIVERING, DELIVERED dùng layout còn lại
                return VIEW_TYPE_DELIVERING;
            }
        }

        @NonNull
        @Override
        public ShopOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v;

            // Inflate layout chính xác dựa trên viewType
            if (viewType == VIEW_TYPE_NOT_YET_DELIVERING) {
                v = inflater.inflate(R.layout.item_shop_not_yet_delivering_order, parent, false);
            } else { // VIEW_TYPE_DELIVERING
                v = inflater.inflate(R.layout.item_shop_delivering_order, parent, false);
            }
            return new ShopOrderViewHolder(v, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull ShopOrderViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        // ViewHolder này có thể dùng chung cho cả 2 layout vì ID của view giống hệt nhau
        static class ShopOrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPrice, tvQuantity, tvStatus, tvDescription;
            ImageView imgProduct; // <-- Thêm ImageView
            final OnShopOrderClick listener;

            ShopOrderViewHolder(@NonNull View itemView, OnShopOrderClick listener) {
                super(itemView);
                this.listener = listener;
                imgProduct = itemView.findViewById(R.id.imgProduct); // <-- Ánh xạ
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvDescription = itemView.findViewById(R.id.tvDescription);
            }

            // Hàm bind đã sửa (giống như code bạn đã dán, đã chính xác)
            void bind(final ShopOrder shopOrder) {

                // Gán dữ liệu chung của đơn hàng
                tvStatus.setText(shopOrder.getStatusText());
                tvDescription.setText(shopOrder.getDescription());
                tvPrice.setText(shopOrder.getTotalPrice());

                // Lấy thông tin từ SẢN PHẨM ĐẦU TIÊN
                if (shopOrder.getItems() != null && !shopOrder.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = shopOrder.getItems().get(0);

                    tvTitle.setText(firstProduct.getTitle());
                    // Cập nhật text cho tvQuantity (layout XML của bạn là "Số lượng: 1")
                    tvQuantity.setText("Số lượng: " + firstProduct.getQuantity());
                    imgProduct.setImageResource(firstProduct.getImageRes()); // Gán ảnh

                } else {
                    // Trường hợp dự phòng
                    tvTitle.setText("Đơn hàng lỗi");
                    tvQuantity.setText("Số lượng: 0");
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null)
                        listener.openDetail(shopOrder.getId(), shopOrder.getDeliveryStatus());
                });
            }
        }
    }
}