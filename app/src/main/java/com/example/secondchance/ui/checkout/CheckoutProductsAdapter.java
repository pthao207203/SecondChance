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
import com.example.secondchance.data.remote.CartApi; // ✅ Thay đổi import

import java.util.ArrayList;
import java.util.List;

public class CheckoutProductsAdapter extends RecyclerView.Adapter<CheckoutProductsAdapter.ProductViewHolder> {

    private List<CartApi.CartItem> products; // ✅ Thay đổi type

    public CheckoutProductsAdapter(List<CartApi.CartItem> selectedProducts) {
        if (selectedProducts != null) {
            this.products = selectedProducts;
        } else {
            this.products = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        CartApi.CartItem product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProductImage;
        private TextView tvProductName, tvProductPrice, tvProductDescription;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
        }

        public void bind(CartApi.CartItem product) {
            tvProductName.setText(product.getName());

            // Format price với dấu chấm
            String priceFormatted = String.format("%,d", product.getTotalPrice()).replace(",", ".");
            tvProductPrice.setText("₫ " + priceFormatted);

            tvProductDescription.setText(product.getDescription());

            // ✅ Load image với Glide
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.color.grayLight)
                        .error(R.color.grayLight)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setBackgroundColor(itemView.getContext()
                        .getResources().getColor(R.color.grayLight, null));
            }
        }
    }
}