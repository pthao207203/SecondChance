package com.example.secondchance.ui.negotiation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemNegotiationCompletedCardBinding;

import java.util.List;

public class NegotiationCompletedAdapter
  extends RecyclerView.Adapter<NegotiationCompletedAdapter.ViewHolder> {
    
    private final List<NegotiationCompleted> completedList;
    
    public NegotiationCompletedAdapter(List<NegotiationCompleted> completedList) {
        this.completedList = completedList;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemNegotiationCompletedCardBinding binding =
          ItemNegotiationCompletedCardBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NegotiationCompleted item = completedList.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return completedList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNegotiationCompletedCardBinding binding;
        
        public ViewHolder(ItemNegotiationCompletedCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(NegotiationCompleted item) {
            // ===== TEXT =====
            binding.userName.setText(item.getUserName());
            binding.productDate.setText(item.getProductDate());
            binding.negotiationAmount.setText(item.getNegotiationRound());
            binding.tvTitle.setText(item.getTitle());
            binding.tvPrice.setText(item.getPrice());
            binding.tvQuantity.setText(item.getQuantity());
            binding.tvSubtitleFixed.setText(item.getFixedPriceText());
            binding.tvSubtitleDate.setText(item.getCreatedDate());
            binding.tvReply.setText(item.getReplyMessage());
            
            // ===== ẢNH AVATAR / SẢN PHẨM =====
            // Avatar user
            if (binding.imgUserAvatar != null) {
                Glide.with(binding.getRoot().getContext())
                  .load(item.getUserAvatarUrl())
                  .placeholder(R.drawable.avatar1)
                  .error(R.drawable.avatar1)
                  .into(binding.imgUserAvatar);
            }
            
            
            // Ảnh sản phẩm
            if (binding.imgProduct != null) {
                Glide.with(binding.getRoot().getContext())
                  .load(item.getProductImageUrl())
                  .placeholder(R.drawable.giohoa1)
                  .error(R.drawable.giohoa1)
                  .into(binding.imgProduct);
            }
            
            // ===== NHẮN TIN VỚI SHOP =====
            if (binding.btnChat != null) {
                binding.btnChat.setOnClickListener(v -> {
                    if (item.getShopId() == null || item.getShopId().isEmpty()) return;
                    
                    Bundle args = new Bundle();
                    args.putString("partnerId", item.getShopId());
                    args.putString("partnerName", item.getShopName());
                    args.putString("partnerAvatar", item.getShopAvatarUrl());
                    
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_trade_to_messageDetail, args);
                });
            }
        }
    }
}
