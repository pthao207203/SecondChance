// NegotiationAcceptedAdapter.java
package com.example.secondchance.ui.negotiation;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemNegotiationAcceptedCardBinding;
import com.google.android.material.button.MaterialButton;

public class NegotiationAcceptedAdapter
  extends ListAdapter<NegotiationAccepted, NegotiationAcceptedAdapter.ViewHolder> {
    
    public NegotiationAcceptedAdapter() {
        super(new DiffCallback());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNegotiationAcceptedCardBinding binding = ItemNegotiationAcceptedCardBinding.inflate(
          LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
    
    // KHÔNG static để dùng getAdapterPosition() nếu cần
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNegotiationAcceptedCardBinding binding;
        
        ViewHolder(ItemNegotiationAcceptedCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        void bind(NegotiationAccepted item) {
            // ========== TEXT ==========
            // Header trên cùng
            binding.productName.setText(item.getUserName());          // tên người mua (Fish can Fly)
            binding.productDate.setText(item.getDate());              // 18/02/2025
            binding.negotiationAmount.setText(item.getNegotiationText()); // "Thương lượng lần 1"
            
            // Card sản phẩm
            binding.tvTitle.setText(item.getProductTitle());          // "Giỏ gỗ cắm hoa"
            binding.tvPrice.setText(item.getPrice());                 // "50.000"
            binding.tvQuantity.setText("x" + item.getQuantity());     // "x1"
            binding.tvSubtitleFixed.setText("Giá cố định");
            binding.tvSubtitleDate.setText("Đã tạo ngày: " + item.getCreatedDate());
            
            // Header shop + reply
            binding.tvShopName.setText(item.getShopName());
            binding.tvShopDate.setText(item.getReplyDate());
            binding.tvReply.setText(item.getReplyMessage());
            
            // ========== ẢNH ==========
            // Avatar user
            Glide.with(binding.getRoot().getContext())
              .load(item.getUserAvatarUrl())
              .placeholder(R.drawable.avatar1)
              .error(R.drawable.avatar1)
              .into(binding.imgUserAvatar);
            
            // Avatar shop
            Glide.with(binding.getRoot().getContext())
              .load(item.getShopAvatarUrl())
              .placeholder(R.drawable.avatar2)
              .error(R.drawable.avatar2)
              .into(binding.imgShopAvatar);
            
            // Ảnh sản phẩm
            Glide.with(binding.getRoot().getContext())
              .load(item.getProductImageUrl())
              .placeholder(R.drawable.giohoa1)
              .error(R.drawable.giohoa1)
              .into(binding.imgProduct);
            
            // ========== NHẮN TIN ==========
            binding.btnChat.setOnClickListener(v -> {
                if (item.getShopId() == null || item.getShopId().isEmpty()) return;
                
                Bundle args = new Bundle();
                args.putString("partnerId", item.getShopId());
                args.putString("partnerName", item.getShopName());
                args.putString("partnerAvatar", item.getShopAvatarUrl());
                
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_trade_to_messageDetail, args);
            });
            
            // ========== NÚT THANH TOÁN ==========
            MaterialButton btn = binding.btnPayNow;
            
            if (item.isPaid()) {
                // ĐÃ THANH TOÁN
                btn.setText("Đã thanh toán");
                btn.setBackgroundTintList(ColorStateList.valueOf(
                  ContextCompat.getColor(btn.getContext(), R.color.grayDay)
                ));
                btn.setTextColor(ContextCompat.getColor(btn.getContext(), R.color.darkerDay));
                btn.setStrokeColor(ColorStateList.valueOf(
                  ContextCompat.getColor(btn.getContext(), R.color.grayDay)
                ));
                btn.setClickable(false);
                btn.setFocusable(false);
            } else {
                // CHƯA THANH TOÁN → cho phép "Thanh toán ngay"
                btn.setText("Thanh toán ngay");
                btn.setBackgroundTintList(ColorStateList.valueOf(
                  ContextCompat.getColor(btn.getContext(), R.color.normalDay)
                ));
                btn.setTextColor(ContextCompat.getColor(btn.getContext(), R.color.whiteDay));
                btn.setStrokeColor(ColorStateList.valueOf(
                  ContextCompat.getColor(btn.getContext(), R.color.normalDay)
                ));
                btn.setClickable(true);
                btn.setFocusable(true);
                
                btn.setOnClickListener(v -> {
                    String productId = item.getId();
                    if (productId == null || productId.isEmpty()) {
                        Log.e("NegotiationAcceptedAdapter",
                          "productId rỗng, không thể chuyển sang Checkout");
                        return;
                    }
                    
                    int quantity = item.getQuantity() > 0 ? item.getQuantity() : 1;
                    
                    Bundle args = new Bundle();
                    args.putString("productId", productId);
                    args.putInt("quantity", quantity);
                    // KHÔNG cần selectedItems khi thanh toán 1 sản phẩm
                    // args.putSerializable("selectedItems", null);  ← có thể bỏ luôn
                    
                    Log.d("NegotiationAcceptedAdapter",
                      "Đi tới navigation_checkout, productId=" + productId + ", qty=" + quantity);
                    
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.navigation_checkout, args);
                });
            }
            
            binding.executePendingBindings();
        }
    }
    
    static class DiffCallback extends DiffUtil.ItemCallback<NegotiationAccepted> {
        @Override
        public boolean areItemsTheSame(@NonNull NegotiationAccepted oldItem,
                                       @NonNull NegotiationAccepted newItem) {
            // Nên dùng id thương lượng nếu có (ví dụ negotiationId)
            // return oldItem.getNegotiationId().equals(newItem.getNegotiationId());
            return oldItem == newItem;
        }
        
        @Override
        public boolean areContentsTheSame(@NonNull NegotiationAccepted oldItem,
                                          @NonNull NegotiationAccepted newItem) {
            return oldItem.isPaid() == newItem.isPaid()
              && oldItem.getUserName().equals(newItem.getUserName())
              && oldItem.getProductTitle().equals(newItem.getProductTitle());
        }
    }
}
