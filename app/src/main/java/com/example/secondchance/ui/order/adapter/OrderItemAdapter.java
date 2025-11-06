package com.example.secondchance.ui.order.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.secondchance.R;
import com.example.secondchance.data.model.OrderItem;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private final Context context;
    private final List<OrderItem> itemList;
    
    public OrderItemAdapter(Context context, List<OrderItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }
    
    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_detail_order, parent, false);
        return new OrderItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        holder.bind(itemList.get(position));
    }
    
    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }
    
    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProduct;
        TextView tvTitle, tvDesc, tvPrice;
        
        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
        
        void bind(OrderItem item) {
            tvTitle.setText(nullSafe(item.getTitle()));
            tvDesc.setText(nullSafe(item.getDescription()));
            tvPrice.setText(nullSafe(item.getPrice()));
            
            String url = item.getImageUrl();
            int resId = item.getImageResId();
            
            if (url != null && !url.isEmpty()) {
                Glide.with(ivProduct.getContext())
                  .load(url)
                  .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                  .into(ivProduct);
            } else if (resId != 0) {
                ivProduct.setImageResource(resId);
            } else {
            }
            
            // TODO: itemView.setOnClickListener(...) nếu cần
        }
        
        private String nullSafe(String s) { return s == null ? "" : s; }
    }
}
