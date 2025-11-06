package com.example.secondchance.ui.order.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.secondchance.data.model.OrderProduct;
import java.util.List;

public class RefundOrderItemAdapter extends RecyclerView.Adapter<RefundOrderItemAdapter.RefundItemViewHolder> {

    private final List<OrderProduct> productList;
    private final Context context;

    public RefundOrderItemAdapter(Context context, List<OrderProduct> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public RefundItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_detail_order, parent, false);
        return new RefundItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RefundItemViewHolder holder, int position) {
        OrderProduct product = productList.get(position);

        holder.tvTitle.setText(product.getTitle());
        holder.tvDesc.setText(product.getSubtitle());
        holder.tvPrice.setText(product.getPrice());
        holder.ivProduct.setImageResource(product.getImageRes());

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class RefundItemViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProduct;
        TextView tvTitle;
        TextView tvDesc;
        TextView tvPrice;

        public RefundItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
