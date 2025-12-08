package com.example.secondchance.ui.negotiation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class NegotiationCancelledAdapter
  extends RecyclerView.Adapter<NegotiationCancelledAdapter.ViewHolder> {
    
    public interface OnContinueNegotiationClickListener {
        void onContinueClick(NegotiationCancelled item);
    }
    
    private final List<NegotiationCancelled> negotiationList;
    private final OnContinueNegotiationClickListener continueListener;
    
    public NegotiationCancelledAdapter(List<NegotiationCancelled> negotiationList,
                                       OnContinueNegotiationClickListener listener) {
        this.negotiationList = negotiationList;
        this.continueListener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_negotiation_cancelled_card, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NegotiationCancelled item = negotiationList.get(position);
        
        // ====== TEXT ======
        holder.productName.setText(item.getProductName());
        holder.productDate.setText(item.getProductDate());
        holder.negotiationRound.setText(item.getNegotiationRound());
        holder.title.setText(item.getTitle());
        holder.price.setText(item.getPrice());
        holder.quantity.setText(item.getQuantity());
        holder.fixedPriceText.setText(item.getFixedPriceText());
        holder.createdDate.setText(item.getCreatedDate());
        holder.shopName.setText(item.getShopName());
        holder.shopDate.setText(item.getShopDate());
        holder.replyMessage.setText(item.getReplyMessage());
        
        // ====== ẢNH ======
        if (holder.imgUserAvatar != null) {
            Glide.with(holder.itemView.getContext())
              .load(item.getUserAvatarUrl())
              .placeholder(R.drawable.avatar1)
              .error(R.drawable.avatar1)
              .into(holder.imgUserAvatar);
        }
        
        if (holder.imgShopAvatar != null) {
            Glide.with(holder.itemView.getContext())
              .load(item.getShopAvatarUrl())
              .placeholder(R.drawable.avatar2)
              .error(R.drawable.avatar2)
              .into(holder.imgShopAvatar);
        }
        
        Glide.with(holder.itemView.getContext())
          .load(item.getProductImageUrl())
          .placeholder(R.drawable.giohoa1)
          .error(R.drawable.giohoa1)
          .into(holder.imgProduct);
        
        // ====== NHẮN TIN VỚI SHOP ======
        holder.btnChat.setOnClickListener(v -> {
            if (item.getShopId() == null || item.getShopId().isEmpty()) return;
            
            Bundle args = new Bundle();
            args.putString("partnerId", item.getShopId());
            args.putString("partnerName", item.getShopName());
            args.putString("partnerAvatar", item.getShopAvatarUrl());
            
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_trade_to_messageDetail, args);
        });
        
        // ====== PHÂN NHÁNH THEO STATUS ======
        if (item.isCancel()) {
            // status = 4 → đã huỷ / quá hạn → dùng IF đầu tiên
            holder.headerShop.setVisibility(View.GONE);
            holder.replyMessage.setText(
              "Thanh toán quá hạn trong vòng 24 giờ.\n" +
                "Giao dịch đã kết thúc và bạn không thể tiếp tục giao dịch."
            );
            holder.btnNegotiationAgain.setText("Thương lượng đã kết thúc");
            holder.btnNegotiationAgain.setEnabled(false);
            holder.btnNegotiationAgain.setBackgroundColor(
              holder.itemView.getResources().getColor(R.color.grayDay)
            );
            holder.btnNegotiationAgain.setTextColor(
              holder.itemView.getResources().getColor(R.color.darkerDay)
            );
            holder.btnNegotiationAgain.setOnClickListener(null);
        } else if (item.isReject()) {
            // status = 3 → bị từ chối nhưng người mua có thể thương lượng tiếp
            holder.headerShop.setVisibility(View.VISIBLE);
            holder.btnNegotiationAgain.setText("Tiếp tục thương lượng");
            holder.btnNegotiationAgain.setEnabled(true);
            holder.btnNegotiationAgain.setBackgroundColor(
              holder.itemView.getResources().getColor(R.color.highLight3)
            );
            holder.btnNegotiationAgain.setTextColor(
              holder.itemView.getResources().getColor(R.color.whiteDay)
            );
            
            holder.btnNegotiationAgain.setOnClickListener(v -> {
                if (continueListener != null) {
                    continueListener.onContinueClick(item);  // gọi về Fragment mở dialog
                }
            });
        } else {
            // fallback
            holder.headerShop.setVisibility(View.GONE);
            holder.btnNegotiationAgain.setText("Thương lượng đã kết thúc");
            holder.btnNegotiationAgain.setEnabled(false);
            holder.btnNegotiationAgain.setOnClickListener(null);
        }
    }
    
    @Override
    public int getItemCount() {
        return negotiationList.size();
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productDate, negotiationRound, title, price, quantity,
          fixedPriceText, createdDate, shopName, shopDate, replyMessage;
        LinearLayout headerShop;
        Button btnNegotiationAgain;
        ShapeableImageView imgProduct;
        ShapeableImageView imgUserAvatar;
        ShapeableImageView imgShopAvatar;
        LinearLayout btnChat;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productDate = itemView.findViewById(R.id.product_date);
            negotiationRound = itemView.findViewById(R.id.negotiation_amount);
            title = itemView.findViewById(R.id.tvTitle);
            price = itemView.findViewById(R.id.tvPrice);
            quantity = itemView.findViewById(R.id.tvQuantity);
            fixedPriceText = itemView.findViewById(R.id.tvSubtitleFixed);
            createdDate = itemView.findViewById(R.id.tvSubtitleDate);
            shopName = itemView.findViewById(R.id.tvShopName);
            shopDate = itemView.findViewById(R.id.tvShopDate);
            replyMessage = itemView.findViewById(R.id.tvReply);
            headerShop = itemView.findViewById(R.id.headerShop);
            btnNegotiationAgain = itemView.findViewById(R.id.NeogtiationAgain);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            imgShopAvatar = itemView.findViewById(R.id.imgShopAvatar);
            btnChat = itemView.findViewById(R.id.btnChat);
        }
    }
}
