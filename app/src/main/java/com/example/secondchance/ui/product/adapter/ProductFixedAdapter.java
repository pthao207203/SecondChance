package com.example.secondchance.ui.product.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.ui.product.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductFixedAdapter extends RecyclerView.Adapter<ProductFixedAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onViewDetailsClick(Product product);
    }

    public ProductFixedAdapter(OnProductClickListener listener) {
        this.productList = new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_fixed, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvPrice, tvPostedDate, tvQuantity, tvType, tvViewDetails;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvPostedDate = itemView.findViewById(R.id.tv_posted_date);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvType = itemView.findViewById(R.id.tv_type);
            tvViewDetails = itemView.findViewById(R.id.tv_view_details);
        }

        public void bind(Product product, OnProductClickListener listener) {
            tvName.setText(product.getName());
            tvPrice.setText(String.format("%,.0f", product.getPrice()));
            tvPostedDate.setText("Posted date: " + product.getPostedDate());
            tvQuantity.setText("Số lượng: " + product.getQuantity());
            tvType.setText(product.getType());

            applyTypeStyle(product.getStatus());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            tvViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(product);
                }
            });
        }

        private void applyTypeStyle(String status) {
            int backgroundColor, textColor;
            switch (status) {
                case "fixed":
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightDay);
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.darkerDay);
                    break;
                case "negotiable":
                    backgroundColor = Color.parseColor("#FFF9E6");
                    textColor = Color.parseColor("#F59E0B");
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightDay);
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.darkerDay);
                    break;
            }
            tvType.setBackgroundColor(backgroundColor);
            tvType.setTextColor(textColor);
        }
    }
}
