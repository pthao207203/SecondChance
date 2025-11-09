package com.example.secondchance.ui.shoporder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // <-- THÊM IMPORT
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
import com.example.secondchance.data.model.ShopOrderProduct;
import com.example.secondchance.databinding.FragmentCancelOrderBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import java.util.ArrayList;
import java.util.List;

public class CancelShopFragment extends Fragment {

    private FragmentCancelOrderBinding binding;
    private CancelOrdersAdapter adapter;
    private final List<ShopOrder> dummyOrderList = new ArrayList<>();

    private SharedViewModel sharedViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCancelOrderBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvCancelOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDummyData();

        adapter = new CancelOrdersAdapter(dummyOrderList, orderId -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("orderId", orderId);
                // Bạn có thể cần sửa action này cho đúng
                nav.navigate(R.id.action_shopOrderFragment_to_canceledShopOrderDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng đã hủy.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvCancelOrders.setAdapter(adapter);

        sharedViewModel.getRefreshLists().observe(getViewLifecycleOwner(), shouldRefresh -> {
            if (shouldRefresh != null && shouldRefresh) {
                Log.d("CancelFragment", "Got refresh signal! Reloading data...");
                loadDummyData();
            }
        });
    }

    // Hàm này đã CHÍNH XÁC
    public void loadDummyData() {
        dummyOrderList.clear();
        // Bước 1: Tạo danh sách sản phẩm cho đơn hàng này
        List<ShopOrderProduct> items = new ArrayList<>();
        items.add(new ShopOrderProduct(
                "P-201", // ID sản phẩm (dummy)
                "Giỏ gỗ cắm hoa", // title
                "Giá cố định", // subtitle (Lấy từ tham số 5 cũ)
                "50.000", // price
                R.drawable.sample_flower, // ảnh (dummy)
                1 // quantity (Đã đổi "x1" thành 1)
        ));

        // Bước 2: Tạo ShopOrder (model mới) với constructor 10 tham số
        dummyOrderList.add(new ShopOrder(
                "CANCELED001", // 1. id
                items, // 2. List<ShopOrderProduct>
                "50.000", // 3. totalPrice
                "17/06/2025", // 4. date (Lấy từ tham số 6 cũ)
                "Đã hủy", // 5. statusText (Lấy từ tham số 7 cũ)
                null, // 6. description
                ShopOrder.ShopOrderType.UNCONFIRMED, // 7. type
                false, // 8. isEvaluated
                null, // 9. refundStatus
                null // 10. deliveryStatus
        ));
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvCancelOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }

    private interface OnOrderClick {
        void openDetail(String orderId);
    }

    private static class CancelOrdersAdapter extends RecyclerView.Adapter<CancelOrdersAdapter.OrderViewHolder> {

        private final List<ShopOrder> items;
        private final OnOrderClick listener;

        CancelOrdersAdapter(List<ShopOrder> items, OnOrderClick listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Đã ĐÚNG: Chỉ inflate 1 layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_canceled_order, parent, false);
            return new OrderViewHolder(view, listener);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items != null ? items.size() : 0;
        }

        // ===================================
        // === VIEWHOLDER ĐÃ SỬA (THÊM ẢNH) ===
        // ===================================
        static class OrderViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct; // <-- THÊM
            TextView tvTitle, tvDate, tvPrice;
            private final OnOrderClick listener;

            OrderViewHolder(@NonNull View itemView, OnOrderClick listener) {
                super(itemView);
                this.listener = listener;
                imgProduct = itemView.findViewById(R.id.imgProduct); // <-- THÊM
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvPrice = itemView.findViewById(R.id.tvPrice);
            }

            // HÀM BIND ĐÃ SỬA (THÊM ẢNH)
            void bind(final ShopOrder order) {

                // Lấy dữ liệu chung của đơn hàng
                tvDate.setText(order.getDate());
                tvPrice.setText(order.getTotalPrice());

                // Lấy dữ liệu từ sản phẩm đầu tiên
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    ShopOrderProduct firstProduct = order.getItems().get(0);
                    tvTitle.setText(firstProduct.getTitle());
                    imgProduct.setImageResource(firstProduct.getImageRes()); // <-- THÊM
                } else {
                    tvTitle.setText("Đơn hàng lỗi"); // Dự phòng
                }

                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.openDetail(order.getId());
                });
            }
        }
    }
}