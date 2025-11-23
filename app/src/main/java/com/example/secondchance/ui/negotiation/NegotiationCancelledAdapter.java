package com.example.secondchance.ui.negotiation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class NegotiationCancelledAdapter extends RecyclerView.Adapter<NegotiationCancelledAdapter.ViewHolder> {

    private final List<NegotiationCancelled> negotiationList;

    public NegotiationCancelledAdapter(List<NegotiationCancelled> negotiationList) {
        this.negotiationList = negotiationList;
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

        if (item.isOverdue()) {
            // Trường hợp 2: Quá hạn thanh toán
            holder.headerShop.setVisibility(View.GONE);
            holder.replyMessage.setText("Thanh toán quá hạn trong vòng 24 giờ.\nGiao dịch đã kết thúc và bạn không thể tiếp tục giao dịch.");
            holder.btnNegotiationAgain.setText("Thương lượng đã kết thúc");
            holder.btnNegotiationAgain.setEnabled(false);
            holder.btnNegotiationAgain.setBackgroundColor(
                    holder.itemView.getResources().getColor(R.color.grayDay)
            );
            holder.btnNegotiationAgain.setTextColor(
                    holder.itemView.getResources().getColor(R.color.darkerDay)
            );
        } else {
            // Trường hợp 1: Bị shop từ chối
            holder.headerShop.setVisibility(View.VISIBLE);
            holder.btnNegotiationAgain.setText("Tiếp tục thương lượng");
            holder.btnNegotiationAgain.setEnabled(true);
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
        }
    }
}
