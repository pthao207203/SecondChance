package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.secondchance.data.model.Order;
import com.example.secondchance.databinding.FragmentDeliveringBinding;

import java.util.ArrayList;
import java.util.List;

public class DeliveringFragment extends Fragment {
    
    private FragmentDeliveringBinding binding;
    private DeliveringAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();
    
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
        
        // Điều hướng trực tiếp bằng NavController (không phụ thuộc parent)
        adapter = new DeliveringAdapter(orderList, (orderId, deliveryStatus) -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("orderId", orderId);
                if (deliveryStatus != null) {
                    args.putSerializable("deliveryStatus", deliveryStatus); // enum DeliveryOverallStatus
                }
                nav.navigate(R.id.action_orderFragment_to_deliveringOrderDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể mở chi tiết đơn giao.", Toast.LENGTH_SHORT).show();
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
    
    private void loadDummyData() {
        orderList.clear();
        orderList.add(new Order("DELIV001", "Nhẫn kim cương hữu hạn", "₫ 50.000", "x1",
          "Giá cố định", "17/06/2025", "Đã đến bưu cục...", "Giỏ hoa loại 1 new 99%...",
          null, false, null, Order.DeliveryOverallStatus.AT_POST_OFFICE));
        orderList.add(new Order("DELIV002", "Vòng tay vàng 24K", "₫ 150.000", "x2",
          "Giá cố định", "17/06/2025", "Đang trên đường giao...", "Giỏ hoa loại 1 new 99%...",
          null, false, null, Order.DeliveryOverallStatus.DELIVERING));
        orderList.add(new Order("DELIV003", "Bông tai ngọc trai", "₫ 75.000", "x1",
          "Giá cố định", "17/06/2025", "Đã đến bưu cục...", "Giỏ hoa loại 1 new 99%...",
          null, false, null, Order.DeliveryOverallStatus.AT_POST_OFFICE));
        
        if (adapter != null) adapter.notifyDataSetChanged();
    }
    
    // ================= Adapter =================
    
    private interface OnOrderClick {
        void openDetail(String orderId, @Nullable Order.DeliveryOverallStatus deliveryStatus);
    }
    
    private static class DeliveringAdapter extends RecyclerView.Adapter<DeliveringAdapter.OrderViewHolder> {
        
        private final List<Order> items;
        private final OnOrderClick listener;
        
        DeliveringAdapter(List<Order> items, OnOrderClick listener) {
            this.items = items;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
              .inflate(R.layout.item_delivering_order, parent, false);
            return new OrderViewHolder(v, listener);
        }
        
        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            holder.bind(items.get(position));
        }
        
        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }
        
        static class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvPrice, tvQuantity, tvStatus, tvDescription;
            final OnOrderClick listener;
            
            OrderViewHolder(@NonNull View itemView, OnOrderClick listener) {
                super(itemView);
                this.listener = listener;
                tvTitle       = itemView.findViewById(R.id.tvTitle);
                tvPrice       = itemView.findViewById(R.id.tvPrice);
                tvQuantity    = itemView.findViewById(R.id.tvQuantity);
                tvStatus      = itemView.findViewById(R.id.tvStatus);
                tvDescription = itemView.findViewById(R.id.tvDescription);
            }
            
            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvQuantity.setText(order.getQuantity());
                tvStatus.setText(order.getStatusText());
                tvDescription.setText(order.getDescription());
                
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.openDetail(order.getId(), order.getDeliveryStatus());
                });
            }
        }
    }
}
