package com.example.secondchance.ui.order.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.imageview.ShapeableImageView;
import com.example.secondchance.R;
import com.example.secondchance.data.model.OrderItem;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {
    private Context context;
    private List<OrderItem> itemList;
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
        OrderItem item = itemList.get(position);
        holder.bind(item);
    }
    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    // Lớp ViewHolder
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

        // gán dữ liệu
        void bind(OrderItem item) {
            tvTitle.setText(item.getTitle());
            tvDesc.setText(item.getDescription());
            tvPrice.setText(item.getPrice());
            // TODO: Gán ảnh cho ivProduct (dùng Glide/Picasso nếu là URL)
            ivProduct.setImageResource(item.getImageResId());

            // TODO: Thêm setOnClickListener cho itemView nếu cần
        }
    }
}