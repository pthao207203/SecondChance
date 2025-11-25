package com.example.secondchance.ui.product.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // ⭐ nhớ thêm dependency Glide trong build.gradle
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
        this.productList = (products != null) ? products : new ArrayList<>();
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
            // Tên
            tvName.setText(product.getName());
            
            // Giá (nếu getPrice() là double/float thì ok, nếu là int mà lỗi format thì sửa lại)
            try {
                tvPrice.setText(String.format("%,.0f", product.getPrice()));
            } catch (Exception e) {
                tvPrice.setText(String.valueOf(product.getPrice()));
            }
            
            // Posted date (có thể null, thì hiện chuỗi rỗng)
            String posted = product.getPostedDate();
            if (posted == null) posted = "";
            tvPostedDate.setText("Posted date: " + posted);
            
            // Số lượng
            tvQuantity.setText("Số lượng: " + product.getQuantity());
            
            // Type text (fixed/auction/…) – nếu bạn muốn label tiếng Việt thì có thể map lại
            String type = product.getType();
            tvType.setText(type != null ? type : "");
            
            // ⭐ LOAD ẢNH TỪ URL (Cloudinary)
            if (product.getImageUrls() != null && !product.getImageUrls().isEmpty()) {
                String imageUrl = product.getImageUrls().get(0);
                
                Glide.with(itemView.getContext())
                  .load(imageUrl)
                  .placeholder(R.drawable.ic_launcher_background) // thay bằng placeholder riêng nếu có
                  .error(R.drawable.ic_launcher_background)
                  .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.ic_launcher_background);
            }
            
            // Style type (màu nền + màu chữ)
            applyTypeStyle(type);
            
            // Click toàn bộ item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
            
            // Click "Xem chi tiết"
            if (tvViewDetails != null) {
                tvViewDetails.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewDetailsClick(product);
                    }
                });
            }
        }
        
        private void applyTypeStyle(String status) {
            // ⭐ tránh NPE khi status = null
            if (status == null) {
                status = "";
            }
            
            int backgroundColor;
            int textColor = ContextCompat.getColor(itemView.getContext(), R.color.darkerDay);
            
            switch (status) {
                case "fixed":
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightDay);
                    break;
                case "negotiable":
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightHoverDay);
                    break;
                case "auction":
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightActiveDay);
                    break;
                case "deleted":
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightHoverDay);
                    break;
                default:
                    backgroundColor = ContextCompat.getColor(itemView.getContext(), R.color.lightHoverDay);
                    break;
            }
            
            if (tvType != null) {
                tvType.setBackgroundColor(backgroundColor);
                tvType.setTextColor(textColor);
            }
        }
    }
}
