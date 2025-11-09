package com.example.secondchance.ui.shoporder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.ViewModelProvider;
import com.example.secondchance.R;
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct; // <-- Import
import com.example.secondchance.databinding.FragmentRefundOrderBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import java.util.ArrayList;
import java.util.List;

public class RefundShopFragment extends Fragment {

    private FragmentRefundOrderBinding binding;
    private RefundShopOrdersAdapter adapter;
    private final List<ShopOrder> dummyOrderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRefundOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDummyData(); // Sẽ gọi hàm đã sửa

        adapter = new RefundShopOrdersAdapter(dummyOrderList, (orderId, refundStatus) -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("orderId", orderId);
                if (refundStatus != null) {
                    args.putSerializable("refundStatus", refundStatus);
                }

                // === LỖI ĐIỀU HƯỚNG ĐÃ SỬA ===
                nav.navigate(R.id.action_shopOrderFragment_to_refundShopOrderDetailFragment, args);

            } catch (Exception e) {
                Log.e("RefundFragment", "Navigate failed", e);
                Toast.makeText(requireContext(), "Không thể mở chi tiết hoàn trả.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvDeliveringOrders.setAdapter(adapter);

        observeViewModel();
    }

    private void observeViewModel() {
        if (sharedViewModel == null) return;

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d("RefundFragment", "Nhận lệnh refresh, tải lại dữ liệu...");
                loadDummyData();
                sharedViewModel.clearRefreshRequest();
            }
        });
    }

    // ===================================
    // === HÀM loatDummyData ĐÃ CẬP NHẬT (THEO LOGIC MỚI) ===
    // ===================================
    private void loadDummyData() {
        dummyOrderList.clear();

        // --- ĐƠN 1: NOT_CONFIRMED (Chưa xác nhận) ---
        List<ShopOrderProduct> items1 = new ArrayList<>();
        items1.add(new ShopOrderProduct("P-401", "Giỏ gỗ cắm hoa", null, "50.000", R.drawable.sample_flower, 1));
        dummyOrderList.add(new ShopOrder("REFUND001", items1, "50.000", "Đã giao 17/6/2025", "Bạn đã từ chối",
                null, null, false,
                ShopOrder.RefundStatus.NOT_CONFIRMED, ShopOrder.DeliveryOverallStatus.DELIVERED));

        // --- ĐƠN 2: CONFIRMED (Đã xác nhận) ---
        List<ShopOrderProduct> items2 = new ArrayList<>();
        items2.add(new ShopOrderProduct("P-402", "Tranh sơn mài", null, "250.000", R.drawable.sample_flower, 1));
        dummyOrderList.add(new ShopOrder("REFUND002", items2, "250.000", "Đã giao 19/6/2025", "Bạn chưa xác nhận",
                null, null, false,
                ShopOrder.RefundStatus.CONFIRMED, ShopOrder.DeliveryOverallStatus.DELIVERED));

        // --- ĐƠN 3: DELIVERING (Đang vận chuyển về) ---
        List<ShopOrderProduct> items3 = new ArrayList<>();
        items3.add(new ShopOrderProduct("P-403", "Bình gốm cổ", null, "150.000", R.drawable.sample_flower, 1));
        dummyOrderList.add(new ShopOrder("REFUND003", items3, "150.000", "Đã giao 18/6/2025", "Đang vận chuyển về",
                null, null, false,
                ShopOrder.RefundStatus.DELIVERING, ShopOrder.DeliveryOverallStatus.DELIVERED));

        // --- ĐƠN 4: SUCCESSFUL (Đã nhận lại hàng) ---
        List<ShopOrderProduct> items4 = new ArrayList<>();
        items4.add(new ShopOrderProduct("P-404", "Nhẫn kim cương", null, "500.000", R.drawable.sample_flower, 1));
        dummyOrderList.add(new ShopOrder("REFUND004", items4, "500.000", "Đã giao 20/6/2025", "Đã nhận lại hàng",
                null, null, false,
                ShopOrder.RefundStatus.SUCCESSFUL, ShopOrder.DeliveryOverallStatus.DELIVERED));

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvDeliveringOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnShopOrderClickListener {
        void onClick(String orderId, @Nullable ShopOrder.RefundStatus refundStatus);
    }

    // ===================================
    // === ADAPTER ĐÃ SỬA LOGIC HOÀN TOÀN ===
    // ===================================
    private static class RefundShopOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<ShopOrder> items;
        private final OnShopOrderClickListener listener;

        // === 4 VIEW TYPE MỚI ===
        private static final int VIEW_TYPE_NOT_CONFIRMED = 1;
        private static final int VIEW_TYPE_CONFIRMED     = 2;
        private static final int VIEW_TYPE_DELIVERING    = 3;
        private static final int VIEW_TYPE_SUCCESSFUL    = 4;

        RefundShopOrdersAdapter(List<ShopOrder> items, OnShopOrderClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        // === SỬA getItemViewType ===
        @Override
        public int getItemViewType(int position) {
            ShopOrder order = items.get(position);
            if (order == null || order.getRefundStatus() == null) return VIEW_TYPE_NOT_CONFIRMED;
            switch (order.getRefundStatus()) {
                case CONFIRMED:  return VIEW_TYPE_CONFIRMED;
                case DELIVERING: return VIEW_TYPE_DELIVERING; // <-- ĐÃ SỬA
                case SUCCESSFUL: return VIEW_TYPE_SUCCESSFUL;
                case NOT_CONFIRMED:
                default:         return VIEW_TYPE_NOT_CONFIRMED;
            }
        }

        // === SỬA onCreateViewHolder (MAP ĐÚNG LAYOUT) ===
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            int layoutId;
            switch (viewType) {
                case VIEW_TYPE_CONFIRMED:
                    layoutId = R.layout.item_shop_refund_confirmed; // <-- ĐÃ SỬA
                    break;
                case VIEW_TYPE_DELIVERING:
                    layoutId = R.layout.item_shop_refund_delivering; // <-- ĐÃ SỬA
                    break;
                case VIEW_TYPE_SUCCESSFUL:
                    layoutId = R.layout.item_shop_refund_successful;
                    break;
                case VIEW_TYPE_NOT_CONFIRMED:
                default:
                    layoutId = R.layout.item_shop_refund_not_confirmed;
                    break;
            }
            View v = inflater.inflate(layoutId, parent, false);
            return new RefundViewHolder(v, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ((RefundViewHolder) holder).bind(items.get(position));
        }

        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }

        // ViewHolder (Đã chính xác, dùng chung cho cả 4 layout)
        private static class RefundViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;
            final OnShopOrderClickListener listener;

            RefundViewHolder(@NonNull View itemView, OnShopOrderClickListener listener) {
                super(itemView);
                this.listener = listener;
                imgProduct         = itemView.findViewById(R.id.imgProduct);
                imgViewInvoiceArrow= itemView.findViewById(R.id.imgViewInvoiceArrow);
                tvTitle            = itemView.findViewById(R.id.tvTitle);
                tvPrice            = itemView.findViewById(R.id.tvPrice);
                tvSubtitleDate     = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatusReview     = itemView.findViewById(R.id.tvStatusReview);
                tvViewInvoiceText  = itemView.findViewById(R.id.tvViewInvoiceText);
            }

            // Hàm bind (Đã chính xác)
            void bind(final ShopOrder order) {
                tvSubtitleDate.setText(order.getDate());
                tvStatusReview.setText(order.getStatusText());
                tvPrice.setText(order.getTotalPrice());

                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = order.getItems().get(0);
                    tvTitle.setText(firstProduct.getTitle());
                    imgProduct.setImageResource(firstProduct.getImageRes());
                } else {
                    tvTitle.setText("Đơn hàng lỗi");
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onClick(order.getId(), order.getRefundStatus());
                });
            }
        }
    }
}