package com.example.secondchance.ui.order;

import android.os.Bundle;
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

import com.example.secondchance.R;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.databinding.FragmentRefundOrderBinding;

import java.util.ArrayList;
import java.util.List;

public class RefundFragment extends Fragment {
    
    private FragmentRefundOrderBinding binding;
    private RefundOrdersAdapter adapter;
    private final List<Order> dummyOrderList = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRefundOrderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.rvDeliveringOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        
        loadDummyData();
        
        // Điều hướng trực tiếp từ fragment qua NavController (không gọi parent)
        adapter = new RefundOrdersAdapter(dummyOrderList, (orderId, refundStatus) -> {
            try {
                NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                Bundle args = new Bundle();
                args.putString("orderId", orderId);
                if (refundStatus != null) {
                    args.putSerializable("refundStatus", refundStatus); // enum RefundStatus
                }
                nav.navigate(R.id.action_orderFragment_to_refundOrderDetailFragment, args);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "Không thể mở chi tiết hoàn trả.", Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.rvDeliveringOrders.setAdapter(adapter);
    }
    
    private void loadDummyData() {
        dummyOrderList.clear();
        dummyOrderList.add(new Order("REFUND001", "Giỏ gỗ cắm hoa", "₫ 50.000", null, null,
          "Đã giao 17/6/2025", "Chưa xác nhận", null, null, false,
          Order.RefundStatus.NOT_CONFIRMED, Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("REFUND002", "Tranh sơn mài", "₫ 250.000", null, null,
          "Đã giao 19/6/2025", "Đã xác nhận", null, null, false,
          Order.RefundStatus.CONFIRMED, Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("REFUND003", "Bình gốm cổ", "₫ 150.000", null, null,
          "Đã giao 18/6/2025", "Đã từ chối", null, null, false,
          Order.RefundStatus.REJECTED, Order.DeliveryOverallStatus.DELIVERED));
        dummyOrderList.add(new Order("REFUND004", "Nhẫn kim cương", "₫ 500.000", null, null,
          "Đã giao 20/6/2025", "Hoàn trả thành công", null, null, false,
          Order.RefundStatus.SUCCESSFUL, Order.DeliveryOverallStatus.DELIVERED));
        
        if (adapter != null) adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvDeliveringOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }
    
    // ================= Adapter =================
    
    /** Callback click item. */
    private interface OnOrderClickListener {
        void onClick(String orderId, @Nullable Order.RefundStatus refundStatus);
    }
    
    /** Adapter hỗ trợ 4 view type theo RefundStatus. */
    private static class RefundOrdersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        
        private final List<Order> items;
        private final OnOrderClickListener listener;
        
        private static final int VIEW_TYPE_NOT_CONFIRMED = 1;
        private static final int VIEW_TYPE_CONFIRMED     = 2;
        private static final int VIEW_TYPE_REJECTED      = 3;
        private static final int VIEW_TYPE_SUCCESSFUL    = 4;
        
        RefundOrdersAdapter(List<Order> items, OnOrderClickListener listener) {
            this.items = items;
            this.listener = listener;
        }
        
        @Override
        public int getItemViewType(int position) {
            Order order = items.get(position);
            if (order == null || order.getRefundStatus() == null) return VIEW_TYPE_NOT_CONFIRMED;
            switch (order.getRefundStatus()) {
                case CONFIRMED:  return VIEW_TYPE_CONFIRMED;
                case REJECTED:   return VIEW_TYPE_REJECTED;
                case SUCCESSFUL: return VIEW_TYPE_SUCCESSFUL;
                case NOT_CONFIRMED:
                default:         return VIEW_TYPE_NOT_CONFIRMED;
            }
        }
        
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            int layoutId;
            switch (viewType) {
                case VIEW_TYPE_CONFIRMED:  layoutId = R.layout.item_refund_confirmed; break;
                case VIEW_TYPE_REJECTED:   layoutId = R.layout.item_refund_rejected; break;
                case VIEW_TYPE_SUCCESSFUL: layoutId = R.layout.item_refund_successful; break;
                case VIEW_TYPE_NOT_CONFIRMED:
                default:                   layoutId = R.layout.item_refund_not_confirmed; break;
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
        
        /** ViewHolder dùng chung cho 4 layout (các id phải trùng giữa các item_*.xml). */
        private static class RefundViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgViewInvoiceArrow;
            TextView tvTitle, tvPrice, tvSubtitleDate, tvStatusReview, tvViewInvoiceText;
            final OnOrderClickListener listener;
            
            RefundViewHolder(@NonNull View itemView, OnOrderClickListener listener) {
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
            
            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvSubtitleDate.setText(order.getDate());
                tvStatusReview.setText(order.getStatusText());
                
                itemView.setOnClickListener(v -> {
                    if (listener != null) listener.onClick(order.getId(), order.getRefundStatus());
                });
            }
        }
    }
}
