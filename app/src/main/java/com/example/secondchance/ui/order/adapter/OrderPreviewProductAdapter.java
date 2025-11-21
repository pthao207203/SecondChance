package com.example.secondchance.ui.order.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.OrderApi;
import java.util.List;
import java.util.Locale;

public class OrderPreviewProductAdapter extends RecyclerView.Adapter<OrderPreviewProductAdapter.ProductViewHolder> {

    private final List<OrderApi.ProductItem> productList;

    public OrderPreviewProductAdapter(List<OrderApi.ProductItem> productList) {
        this.productList = productList;
    }

    public void updateData(List<OrderApi.ProductItem> newProductList) {
        this.productList.clear();
        if (newProductList != null) {
            this.productList.addAll(newProductList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductDescription, tvProductPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
        }

        void bind(OrderApi.ProductItem product) {
            tvProductName.setText(product.name);
            tvProductDescription.setText(product.shortDescription);
            String formattedPrice = String.format(Locale.GERMANY, "%,d", product.lineTotal).replace(",", ".");
            tvProductPrice.setText("đ " + formattedPrice);

            Glide.with(itemView.getContext())
                    .load(product.imageUrl)
                    .placeholder(R.color.grayLight)
                    .into(ivProductImage);
        }
    }
}
