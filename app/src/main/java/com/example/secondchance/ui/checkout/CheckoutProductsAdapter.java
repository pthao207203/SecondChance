package com.example.secondchance.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.dto.response.PreviewOrderResponse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CheckoutProductsAdapter extends RecyclerView.Adapter<CheckoutProductsAdapter.ProductViewHolder> {
    private List<PreviewOrderResponse.PreviewItem> items = new ArrayList<>();

    public void setItems(List<PreviewOrderResponse.ShopGroup> shopGroups) {
        this.items.clear();
        if (shopGroups != null) {
            for (PreviewOrderResponse.ShopGroup group : shopGroups) {
                if (group.items != null) {
                    this.items.addAll(group.items);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        PreviewOrderResponse.PreviewItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage, checkboxItem;
        private TextView tvProductName, tvProductPrice, tvProductQuantity;
        private View layoutDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
            layoutDelete = itemView.findViewById(R.id.layoutDelete);

            if (layoutDelete != null) layoutDelete.setVisibility(View.GONE);
            if (checkboxItem != null) checkboxItem.setVisibility(View.GONE);
        }

        public void bind(PreviewOrderResponse.PreviewItem item) {
            tvProductName.setText(item.name);

            DecimalFormat formatter = new DecimalFormat("#,###");
            tvProductPrice.setText(formatter.format(item.price));

            tvProductQuantity.setText("Số lượng: " + item.qty);

            if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.imageUrl)
                        .into(ivProductImage);
            }
        }
    }
}
