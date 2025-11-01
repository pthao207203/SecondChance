package com.example.secondchance.ui.product.adapter;

import android.graphics.Color;
import android.net.Uri;
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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onViewDetailsClick(Product product);
    }

    public ProductAdapter(OnProductClickListener listener) {
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

        public void bind(final Product product, final OnProductClickListener listener) {
            tvName.setText(product.getName());
            tvPrice.setText(String.format("%,.0f", product.getPrice()));
            tvPostedDate.setText("Posted date: " + product.getPostedDate());
            tvQuantity.setText("Số lượng: " + product.getQuantity());
            tvType.setText(product.getType());

            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                String imageUrl = product.getImageUrls().get(0);
                try {
                    // Try to parse as an integer (resource ID)
                    int resId = Integer.parseInt(imageUrl);
                    imgProduct.setImageResource(resId);
                } catch (NumberFormatException e) {
                    // If it fails, treat it as a URI string
                    imgProduct.setImageURI(Uri.parse(imageUrl));
                }
            }

            applyTypeStyle(product.getStatus());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            if (tvViewDetails != null) {
                tvViewDetails.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewDetailsClick(product);
                    }
                });
            }
        }
        
        private void applyTypeStyle(String status) {
            int backgroundColor, textColor;

            switch (status) {
                case "fixed":
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightDay);
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.darkerDay);
                    break;
                case "negotiable":
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightHoverDay);
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.darkerDay);
                    break;
                case "auction":
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightActiveDay);
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.darkerDay);
                    break;
                 case "deleted":
                     backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightHoverDay);
                     textColor = ContextCompat.getColor(itemView.getContext(), R.color.darkerDay);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightHoverDay);
                    textColor = ContextCompat.getColor(itemView.getContext(), R.color.darkerDay);
                    break;
            }

            if (tvType != null) {
                tvType.setBackgroundColor(backgroundColor);
                tvType.setTextColor(textColor);
            }
        }
    }
}
