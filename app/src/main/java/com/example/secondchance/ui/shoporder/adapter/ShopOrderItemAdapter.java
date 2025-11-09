package com.example.secondchance.ui.shoporder.adapter;

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
import com.example.secondchance.data.model.ShopOrderItem;
import java.util.List;

public class ShopOrderItemAdapter extends RecyclerView.Adapter<ShopOrderItemAdapter.ShopOrderItemViewHolder> {
    private final Context context;
    private final List<ShopOrderItem> itemList;

    public ShopOrderItemAdapter(Context context, List<ShopOrderItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ShopOrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_detail_order, parent, false);
        return new ShopOrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopOrderItemViewHolder holder, int position) {
        holder.bind(itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    class ShopOrderItemViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProduct;
        TextView tvTitle, tvDesc, tvPrice;

        public ShopOrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        void bind(ShopOrderItem item) {
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
            }

            // TODO: itemView.setOnClickListener(...) nếu cần
        }

        private String nullSafe(String s) {
            return s == null ? "" : s;
        }
    }
}
