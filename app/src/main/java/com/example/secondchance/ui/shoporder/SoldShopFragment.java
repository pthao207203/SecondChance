package com.example.secondchance.ui.shoporder;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.databinding.FragmentBoughtOrderBinding; // Bạn có thể cần đổi tên này thành FragmentSoldOrderBinding
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.data.model.ShopOrderProduct;

import java.util.ArrayList;
import java.util.List;

// Đã đổi tên class (bạn đã làm)
public class SoldShopFragment extends Fragment {

    // Bạn có thể cần đổi tên binding này nếu bạn có file layout riêng
    private FragmentBoughtOrderBinding binding;

    // Sửa tên Adapter cho nhất quán
    private SoldShopOrdersAdapter adapter;
    private final List<ShopOrder> dummyShopOrderList = new ArrayList<>();
    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBoughtOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // RecyclerView (ID này có thể cần đổi tên nếu binding của bạn khác)
        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dữ liệu mẫu (Đã đúng)
        loadDummyData();

        // Sửa tên Adapter
        adapter = new SoldShopOrdersAdapter(dummyShopOrderList, (shopOrderId, isEvaluated) -> {
            try {
                NavController nav = Navigation.findNavController(
                        requireActivity(), R.id.nav_host_fragment_activity_main
                );
                Bundle args = new Bundle();
                args.putString("shopOrderId", shopOrderId);
                args.putBoolean("isEvaluated", isEvaluated);
                // Bạn có thể cần sửa action navigation này
                nav.navigate(R.id.action_shopOrderFragment_to_soldShopOrderDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng cửa hàng.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvDeliveringOrders.setAdapter(adapter);
        observeViewModel();
    }

    private void observeViewModel() {
        if (sharedViewModel == null) return;

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d("SoldShopFragment", "Nhận lệnh refresh, tải lại dữ liệu...");
                loadDummyData();
                sharedViewModel.clearRefreshRequest();
            }
        });
    }

    // Hàm này đã CHÍNH XÁC, nó tạo 2 loại đơn (đã/chưa đánh giá)
    private void loadDummyData() {
        dummyShopOrderList.clear();

        // === ĐƠN HÀNG 1 (Chưa đánh giá) ===
        List<ShopOrderProduct> items1 = new ArrayList<>();
        items1.add(new ShopOrderProduct(
                "P-101", "Giỏ gỗ cắm hoa", null, "50.000",
                R.drawable.sample_flower, 1
        ));
        dummyShopOrderList.add(new ShopOrder(
                "SHOP001", items1, "50.000", "Đã giao 17/6/2025", "Chưa đánh giá",
                null, null, false, null, ShopOrder.DeliveryOverallStatus.DELIVERED
        ));

        // === ĐƠN HÀNG 2 (Đã đánh giá) ===
        List<ShopOrderProduct> items2 = new ArrayList<>();
        items2.add(new ShopOrderProduct(
                "P-102", "Tranh sơn mài", null, "250.000",
                R.drawable.sample_flower, 1
        ));
        dummyShopOrderList.add(new ShopOrder(
                "SHOP002", items2, "250.000", "Đã giao 19/6/2025", "Khách đã đánh giá",
                null, null, true, null, ShopOrder.DeliveryOverallStatus.DELIVERED
        ));

        // ... (Bạn có thể thêm đơn 3 nếu muốn)

        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            binding.rvDeliveringOrders.setAdapter(null);
        }
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    // Adapter

    private interface OnShopOrderClickListener {
        void onShopOrderClick(String shopOrderId, boolean isEvaluated);
    }

    // Đã đổi tên Adapter
    private static class SoldShopOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<ShopOrder> items;
        private final OnShopOrderClickListener listener;

        private static final int VIEW_TYPE_NOT_EVALUATED = 1;
        private static final int VIEW_TYPE_EVALUATED = 2;

        SoldShopOrdersAdapter(List<ShopOrder> items, OnShopOrderClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        // Logic này đã ĐÚNG
        @Override
        public int getItemViewType(int position) {
            ShopOrder shopOrder = items.get(position);
            return shopOrder.isEvaluated() ? VIEW_TYPE_EVALUATED : VIEW_TYPE_NOT_EVALUATED;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            // Logic này đã ĐÚNG
            if (viewType == VIEW_TYPE_EVALUATED) {
                View v = inflater.inflate(R.layout.item_shop_sold_evaluated, parent, false);
                return new EvaluatedViewHolder(v, listener);
            } else {
                View v = inflater.inflate(R.layout.item_shop_sold_not_evaluated, parent, false);
                return new NotEvaluatedViewHolder(v, listener);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ShopOrder shopOrder = items.get(position);
            if (holder instanceof EvaluatedViewHolder) {
                ((EvaluatedViewHolder) holder).bind(shopOrder);
            } else if (holder instanceof NotEvaluatedViewHolder) {
                ((NotEvaluatedViewHolder) holder).bind(shopOrder);
            }
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        // ===================================
        // === VIEWHOLDER ĐÃ SỬA LỖI (KHÔNG CÓ tvStatusReview) ===
        // ===================================
        private static class NotEvaluatedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            // XÓA tvStatusReview
            TextView tvTitle, tvPrice, tvSubtitleDate, tvViewInvoiceText;

            private final OnShopOrderClickListener listener;

            NotEvaluatedViewHolder(@NonNull View itemView, OnShopOrderClickListener listener) {
                super(itemView);
                this.listener = listener;
                imgProduct = itemView.findViewById(R.id.imgProduct);
                imgViewInvoiceArrow = itemView.findViewById(R.id.imgViewInvoiceArrow);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                // XÓA findViewById cho tvStatusReview
                tvViewInvoiceText = itemView.findViewById(R.id.tvViewInvoiceText);
            }

            void bind(ShopOrder shopOrder) {
                // Gán thông tin chung của đơn hàng
                tvPrice.setText(shopOrder.getTotalPrice());
                tvSubtitleDate.setText(shopOrder.getDate());
                // XÓA setText cho tvStatusReview

                // Gán thông tin sản phẩm đầu tiên
                if (shopOrder.getItems() != null && !shopOrder.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = shopOrder.getItems().get(0);
                    tvTitle.setText(firstProduct.getTitle());
                    imgProduct.setImageResource(firstProduct.getImageRes());
                } else {
                    tvTitle.setText("Đơn hàng lỗi");
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onShopOrderClick(shopOrder.getId(), shopOrder.isEvaluated());
                });
            }
        }

        // ===================================
        // === VIEWHOLDER NÀY GIỮ NGUYÊN (VÌ CÓ tvStatusReview) ===
        // ===================================
        private static class EvaluatedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;

            private final OnShopOrderClickListener listener;

            EvaluatedViewHolder(@NonNull View itemView, OnShopOrderClickListener listener) {
                super(itemView);
                this.listener = listener;
                imgProduct = itemView.findViewById(R.id.imgProduct);
                imgViewInvoiceArrow = itemView.findViewById(R.id.imgViewInvoiceArrow);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatusReview = itemView.findViewById(R.id.tvStatusReview); // <-- Giữ lại
                tvViewInvoiceText = itemView.findViewById(R.id.tvViewInvoiceText);
            }

            void bind(ShopOrder shopOrder) {
                // Gán thông tin chung của đơn hàng
                tvPrice.setText(shopOrder.getTotalPrice());
                tvSubtitleDate.setText(shopOrder.getDate());
                tvStatusReview.setText(shopOrder.getStatusText()); // <-- Giữ lại

                // Gán thông tin sản phẩm đầu tiên
                if (shopOrder.getItems() != null && !shopOrder.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = shopOrder.getItems().get(0);
                    tvTitle.setText(firstProduct.getTitle());
                    imgProduct.setImageResource(firstProduct.getImageRes());
                } else {
                    tvTitle.setText("Đơn hàng lỗi");
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onShopOrderClick(shopOrder.getId(), shopOrder.isEvaluated());
                });
            }
        }
    }
}