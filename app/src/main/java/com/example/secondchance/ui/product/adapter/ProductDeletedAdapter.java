package com.example.secondchance.ui.product.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.ui.product.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductDeletedAdapter extends RecyclerView.Adapter<ProductDeletedAdapter.DeletedViewHolder> {

    private List<Product> productList;
    private final OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductDeletedAdapter(OnProductClickListener listener) {
        this.productList = new ArrayList<>();
        this.listener = listener;
    }

    public void setProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DeletedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_deleted, parent, false);
        return new DeletedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeletedViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class DeletedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvPrice;
        TextView tvPostedDate;
        TextView tvQuantity;
        TextView tvType;

        public DeletedViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvPostedDate = itemView.findViewById(R.id.tv_posted_date);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvType = itemView.findViewById(R.id.tv_type);
        }

        public void bind(final Product product, final OnProductClickListener listener) {
            tvProductName.setText(product.getName());
            tvPrice.setText(String.format("%,.0f", product.getPrice()));
            tvQuantity.setText("Số lượng: " + product.getQuantity());
            
            // Use tv_posted_date to show Deleted Date
            tvPostedDate.setText("Ngày xóa: " + product.getDeletedDate());
            
            // Use tv_type to show the Original Status
            tvType.setText(product.getOriginalStatus());

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

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
