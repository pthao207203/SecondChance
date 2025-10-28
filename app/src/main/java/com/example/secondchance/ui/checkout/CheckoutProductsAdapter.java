package com.example.secondchance.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.ui.cart.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CheckoutProductsAdapter extends RecyclerView.Adapter<CheckoutProductsAdapter.ProductViewHolder> {

    private List<CartItem> products;



    public CheckoutProductsAdapter(List<CartItem> selectedProducts) {
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
        CartItem product = products.get(position);
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

        public void bind(CartItem product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText("đ " + String.format("%,d", product.getPrice()));
            tvProductDescription.setText(product.getDescription());

            // Load image if you have image URL
            // Glide.with(itemView.getContext()).load(product.getImageUrl()).into(ivProductImage);
        }
    }
}