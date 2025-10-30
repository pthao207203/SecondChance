package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.secondchance.databinding.FragmentConfirmationBinding;

import java.util.ArrayList;
import java.util.List;

public class ConfirmationFragment extends Fragment {
    
    private FragmentConfirmationBinding binding;
    private ConfirmationAdapter adapter;
    private final List<Order> orderList = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        loadDummyData();
        
        // Điều hướng trực tiếp bằng NavController (không phụ thuộc parent)
        adapter = new ConfirmationAdapter(
          orderList,
          (orderId, orderType) -> {
              try {
                  NavController nav = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                  Bundle args = new Bundle();
                  args.putString("orderId", orderId);
                  if (orderType != null) {
                      args.putSerializable("orderType", orderType); // enum Order.OrderType
                  }
                  nav.navigate(R.id.action_orderFragment_to_confirmOrderDetailFragment, args);
              } catch (Exception e) {
                  Log.e("ConfirmationFragment", "Navigate to ConfirmDetail failed", e);
                  Toast.makeText(requireContext(), "Không thể mở chi tiết đơn hàng.", Toast.LENGTH_SHORT).show();
              }
          },
          orderId -> {
              // TODO: thêm logic hủy đơn (call API / navigate)
              Toast.makeText(getContext(), "Hủy đơn: " + orderId, Toast.LENGTH_SHORT).show();
          }
        );
        
        binding.rvOrders.setAdapter(adapter);
    }
    
    private void loadDummyData() {
        orderList.clear();
        orderList.add(new Order("ORD001", "Giỏ gỗ cắm hoa", "₫ 50.000", "x1", "Giá cố định", "17/06/2025", "Chưa xác nhận", null, Order.OrderType.UNCONFIRMED, false, null, Order.DeliveryOverallStatus.DELIVERED));
        orderList.add(new Order("ORD002", "Giỏ gỗ cắm hoa", "₫ 50.000", "x1", "Giá cố định", "17/06/2025", "Đã xác nhận", "Đơn đã xác nhận không thể hủy", Order.OrderType.CONFIRMED_FIXED, false, null, Order.DeliveryOverallStatus.DELIVERED));
        orderList.add(new Order("ORD003", "Giỏ gỗ cắm hoa", "₫ 50.000", "x1", "Đấu giá", "17/06/2025", "Đã xác nhận", "Đơn đấu giá không thể hủy", Order.OrderType.CONFIRMED_AUCTION, false, null, Order.DeliveryOverallStatus.DELIVERED));
        orderList.add(new Order("ORD004", "Bình gốm cổ", "₫ 150.000", "x1", "Giá cố định", "18/06/2025", "Chưa xác nhận", null, Order.OrderType.UNCONFIRMED, false, null, Order.DeliveryOverallStatus.DELIVERED));
        orderList.add(new Order("ORD005", "Tranh sơn mài", "₫ 250.000", "x1", "Đấu giá", "19/06/2025", "Đã xác nhận", "Đơn đấu giá không thể hủy", Order.OrderType.CONFIRMED_AUCTION, false, null, Order.DeliveryOverallStatus.DELIVERED));
        
        if (adapter != null) adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onDestroyView() {
        if (binding != null) binding.rvOrders.setAdapter(null);
        adapter = null;
        binding = null;
        super.onDestroyView();
    }
    
    // ================= Listener interfaces =================
    public interface OnItemClickListener {
        void onItemClick(String orderId, Order.OrderType orderType);
    }
    public interface OnCancelClickListener {
        void onCancelClick(String orderId);
    }
    
    // ================= Adapter =================
    private static class ConfirmationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        
        private final List<Order> items;
        private final OnItemClickListener itemClickListener;
        private final OnCancelClickListener cancelClickListener;
        
        private static final int VIEW_TYPE_UNCONFIRMED = 1;
        private static final int VIEW_TYPE_CONFIRMED_FIXED = 2;
        private static final int VIEW_TYPE_CONFIRMED_AUCTION = 3;
        
        ConfirmationAdapter(List<Order> items, OnItemClickListener itemClickListener, OnCancelClickListener cancelClickListener) {
            this.items = items;
            this.itemClickListener = itemClickListener;
            this.cancelClickListener = cancelClickListener;
        }
        
        @Override
        public int getItemViewType(int position) {
            Order order = items.get(position);
            if (order == null || order.getType() == null) return VIEW_TYPE_UNCONFIRMED;
            switch (order.getType()) {
                case UNCONFIRMED:       return VIEW_TYPE_UNCONFIRMED;
                case CONFIRMED_FIXED:   return VIEW_TYPE_CONFIRMED_FIXED;
                case CONFIRMED_AUCTION: return VIEW_TYPE_CONFIRMED_AUCTION;
                default:                return VIEW_TYPE_UNCONFIRMED;
            }
        }
        
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view;
            switch (viewType) {
                case VIEW_TYPE_UNCONFIRMED:
                    view = inflater.inflate(R.layout.item_unconfirm_order, parent, false);
                    return new UnconfirmedViewHolder(view, itemClickListener, cancelClickListener);
                case VIEW_TYPE_CONFIRMED_FIXED:
                    view = inflater.inflate(R.layout.item_confirm_fixed_order, parent, false);
                    return new ConfirmedFixedViewHolder(view, itemClickListener);
                case VIEW_TYPE_CONFIRMED_AUCTION:
                    view = inflater.inflate(R.layout.item_confirm_auction_order, parent, false);
                    return new ConfirmedAuctionViewHolder(view, itemClickListener);
                default:
                    view = inflater.inflate(R.layout.item_unconfirm_order, parent, false);
                    return new UnconfirmedViewHolder(view, itemClickListener, cancelClickListener);
            }
        }
        
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Order order = items.get(position);
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_UNCONFIRMED:
                    ((UnconfirmedViewHolder) holder).bind(order);
                    break;
                case VIEW_TYPE_CONFIRMED_FIXED:
                    ((ConfirmedFixedViewHolder) holder).bind(order);
                    break;
                case VIEW_TYPE_CONFIRMED_AUCTION:
                    ((ConfirmedAuctionViewHolder) holder).bind(order);
                    break;
            }
        }
        
        @Override
        public int getItemCount() { return items != null ? items.size() : 0; }
        
        // ----- ViewHolder: UNCONFIRMED -----
        private static class UnconfirmedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct, imgDot;
            TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus;
            Button btnCancel;
            
            private final OnItemClickListener itemClickListener;
            private final OnCancelClickListener cancelClickListener;
            
            UnconfirmedViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener, OnCancelClickListener cancelClickListener) {
                super(itemView);
                this.itemClickListener = itemClickListener;
                this.cancelClickListener = cancelClickListener;
                
                imgProduct     = itemView.findViewById(R.id.imgProduct);
                imgDot         = itemView.findViewById(R.id.imgDot);
                tvTitle        = itemView.findViewById(R.id.tvTitle);
                tvPrice        = itemView.findViewById(R.id.tvPrice);
                tvQuantity     = itemView.findViewById(R.id.tvQuantity);
                tvSubtitleFixed= itemView.findViewById(R.id.tvSubtitleFixed);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatus       = itemView.findViewById(R.id.tvStatus);
                btnCancel      = itemView.findViewById(R.id.btnCancel);
            }
            
            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvQuantity.setText(order.getQuantity());
                tvSubtitleFixed.setText(order.getSubtitle());
                tvSubtitleDate.setText(order.getDate());
                tvStatus.setText(order.getStatusText());
                
                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) itemClickListener.onItemClick(order.getId(), order.getType());
                });
                btnCancel.setOnClickListener(v -> {
                    if (cancelClickListener != null) cancelClickListener.onCancelClick(order.getId());
                });
            }
        }
        
        // ----- ViewHolder: CONFIRMED_FIXED -----
        private static class ConfirmedFixedViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus, tvAuctionInfo;
            private final OnItemClickListener itemClickListener;
            
            ConfirmedFixedViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
                super(itemView);
                this.itemClickListener = itemClickListener;
                
                imgProduct     = itemView.findViewById(R.id.imgProduct);
                tvTitle        = itemView.findViewById(R.id.tvTitle);
                tvPrice        = itemView.findViewById(R.id.tvPrice);
                tvQuantity     = itemView.findViewById(R.id.tvQuantity);
                tvSubtitleFixed= itemView.findViewById(R.id.tvSubtitleFixed);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatus       = itemView.findViewById(R.id.tvStatus);
                tvAuctionInfo  = itemView.findViewById(R.id.tvAuctionInfo);
            }
            
            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvQuantity.setText(order.getQuantity());
                tvSubtitleFixed.setText(order.getSubtitle());
                tvSubtitleDate.setText(order.getDate());
                tvStatus.setText(order.getStatusText());
                tvAuctionInfo.setText(order.getDescription());
                
                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) itemClickListener.onItemClick(order.getId(), order.getType());
                });
            }
        }
        
        // ----- ViewHolder: CONFIRMED_AUCTION -----
        private static class ConfirmedAuctionViewHolder extends RecyclerView.ViewHolder {
            ImageView imgProduct;
            TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus, tvAuctionInfo;
            private final OnItemClickListener itemClickListener;
            
            ConfirmedAuctionViewHolder(@NonNull View itemView, OnItemClickListener itemClickListener) {
                super(itemView);
                this.itemClickListener = itemClickListener;
                
                imgProduct     = itemView.findViewById(R.id.imgProduct);
                tvTitle        = itemView.findViewById(R.id.tvTitle);
                tvPrice        = itemView.findViewById(R.id.tvPrice);
                tvQuantity     = itemView.findViewById(R.id.tvQuantity);
                tvSubtitleFixed= itemView.findViewById(R.id.tvSubtitleFixed);
                tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
                tvStatus       = itemView.findViewById(R.id.tvStatus);
                tvAuctionInfo  = itemView.findViewById(R.id.tvAuctionInfo);
            }
            
            void bind(final Order order) {
                tvTitle.setText(order.getTitle());
                tvPrice.setText(order.getPrice());
                tvQuantity.setText(order.getQuantity());
                tvSubtitleFixed.setText(order.getSubtitle());
                tvSubtitleDate.setText(order.getDate());
                tvStatus.setText(order.getStatusText());
                tvAuctionInfo.setText(order.getDescription());
                
                itemView.setOnClickListener(v -> {
                    if (itemClickListener != null) itemClickListener.onItemClick(order.getId(), order.getType());
                });
            }
        }
    }
}
