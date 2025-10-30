package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentDeliveringBinding;
import com.example.secondchance.data.model.Order;
import java.util.ArrayList;
import java.util.List;

public class DeliveringFragment extends Fragment {
    private FragmentDeliveringBinding binding;
    private DeliveringAdapter adapter;
    private List<Order> orderList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeliveringBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        loadDummyData();

        // Khởi tạo Adapter
        adapter = new DeliveringAdapter(orderList);

        //Gán Adapter cho RecyclerView
        binding.rvDeliveringOrders.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadDummyData() {
        orderList.clear();
        orderList.add(new Order("DELIV001", "Nhẫn kim cương hữu hạn", "₫ 50.000", "x1", "Giá cố định", "17/06/2025", "Đã đến bưu cục...", "Giỏ hoa loại 1 new 99%...", null, false, null, Order.DeliveryOverallStatus.AT_POST_OFFICE)); // <<< DeliveryOverallStatus
        orderList.add(new Order("DELIV002", "Vòng tay vàng 24K", "₫ 150.000", "x2", "Giá cố định", "17/06/2025", "Đang trên đường giao...", "Giỏ hoa loại 1 new 99%...", null, false, null, Order.DeliveryOverallStatus.DELIVERING)); // <<< DeliveryOverallStatus
        orderList.add(new Order("DELIV003", "Bông tai ngọc trai", "₫ 75.000", "x1", "Giá cố định", "17/06/2025", "Đã đến bưu cục...", "Giỏ hoa loại 1 new 99%...", null, false, null, Order.DeliveryOverallStatus.AT_POST_OFFICE)); // <<< DeliveryOverallStatus
    }

    private class DeliveringAdapter extends RecyclerView.Adapter<DeliveringAdapter.OrderViewHolder> {
        private List<Order> items;
        DeliveringAdapter(List<Order> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delivering_order, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = items.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPrice, tvQuantity, tvStatus, tvDescription;

            OrderViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvPrice = itemView.findViewById(R.id.tvPrice);
                tvQuantity = itemView.findViewById(R.id.tvQuantity);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                tvDescription = itemView.findViewById(R.id.tvDescription);
                // imgProduct = itemView.findViewById(R.id.imgProduct);
            }

            void bind(final Order order) {
                // Hiển thị dữ liệu từ Order
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvQuantity.setText(order.getQuantity());
                tvStatus.setText(order.getStatusText()); // Hiển thị trạng thái giao hàng
                tvDescription.setText(order.getDescription()); // Hiển thị mô tả
                // TODO: Hiển thị ảnh

                itemView.setOnClickListener(v -> {
                    if (getParentFragment() instanceof OrderFragment) {
                        ((OrderFragment) getParentFragment()).navigateToDetail(
                                order.getId(),
                                R.id.action_orderFragment_to_deliveringOrderDetailFragment,
                                order.getDeliveryStatus()
                        );
                    }
                });
            }
        }
    }
}