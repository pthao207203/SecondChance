package com.example.secondchance.ui.cart;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.CartApi;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final String TAG = "CartAdapter";
    private List<CartApi.CartItem> cartItems;
    private final OnCartItemListener listener;

    public interface OnCartItemListener {
        void onItemChecked(CartApi.CartItem item, boolean isChecked);
        void onViewDetail(CartApi.CartItem item);
        void onItemDeleted(CartApi.CartItem item, int position);
        void onTotalChange(long total); // 🔥 Thêm hàm này để báo ra ngoài
    }

    public CartAdapter(OnCartItemListener listener) {
        this.cartItems = new ArrayList<>();
        this.listener = listener;
    }

    public void updateItems(List<CartApi.CartItem> newItems) {
        this.cartItems.clear();
        if (newItems != null) {
            this.cartItems.addAll(newItems);
        }
        notifyDataSetChanged();
        // Tính lại tiền khi load xong
        if (listener != null) listener.onTotalChange(calculateTotal());
    }

    // 🔥 Hàm lấy danh sách chọn để gửi sang Checkout (Quan trọng)
    public ArrayList<CartApi.CartItem> getSelectedItems() {
        ArrayList<CartApi.CartItem> selectedItems = new ArrayList<>();
        for (CartApi.CartItem item : cartItems) {
            if (item.isSelected) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    // 🔥 Hàm tính tổng tiền
    public long calculateTotal() {
        long total = 0;
        for (CartApi.CartItem item : cartItems) {
            if (item.isSelected) {
                // Lưu ý: getPrice() trả về đơn giá, nhân với qty
                total += (item.getPrice() * item.qty);
            }
        }
        return total;
    }

    public void selectAll(boolean select) {
        for (CartApi.CartItem item : cartItems) {
            item.isSelected = select;
        }
        notifyDataSetChanged();
        if (listener != null) listener.onTotalChange(calculateTotal());
    }

    public boolean areAllItemsSelected() {
        if (cartItems.isEmpty()) return false;
        for (CartApi.CartItem item : cartItems) {
            if (!item.isSelected) return false;
        }
        return true;
    }

    public List<CartApi.CartItem> getItems() {
        return cartItems;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartApi.CartItem item = cartItems.get(position);
        if (item.product == null || item.product.title == null) {
            holder.bindLoadingState();
            fetchProductInfo(item, holder);
        } else {
            holder.bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    private void fetchProductInfo(final CartApi.CartItem item, final CartViewHolder holder) {
        RetrofitProvider.product().getProductById(item.productId).enqueue(new Callback<ProductApi.ProductEnvelope>() {
            @Override
            public void onResponse(Call<ProductApi.ProductEnvelope> call, Response<ProductApi.ProductEnvelope> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                    ProductApi.Product product = response.body().data;

                    if (item.product == null) {
                        item.product = new CartApi.CartItem.ProductInfo();
                    }
                    item.product.id = product.id;
                    item.product.title = product.name;
                    item.product.description = product.description;
                    if (product.media != null && !product.media.isEmpty()) {
                        item.product.imageUrl = product.media.get(0);
                    }

                    if (item.price == 0 && product.price > 0) {
                        item.price = product.price;
                    }

                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        notifyItemChanged(currentPosition);
                    }
                } else {
                    Log.e(TAG, "Failed to fetch product details: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ProductApi.ProductEnvelope> call, Throwable t) {
                Log.e(TAG, "Error fetching product info", t);
            }
        });
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView checkboxItem, ivProductImage;
        private final TextView tvProductName, tvProductPrice, tvProductDescription, tvProductQuantity;
        private final View layoutViewDetail, layoutDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            layoutViewDetail = itemView.findViewById(R.id.layoutViewDetail);
            layoutDelete = itemView.findViewById(R.id.layoutDelete);
        }
        void bindLoadingState() {
            tvProductName.setText("Đang tải...");
            tvProductPrice.setText("0");
            tvProductDescription.setText("");
            tvProductQuantity.setText("");
            ivProductImage.setImageResource(R.color.grayLight);
        }

        public void bind(final CartApi.CartItem item) {
            tvProductName.setText(item.getName());
            tvProductDescription.setText(item.getDescription());
            tvProductQuantity.setText("Số lượng: " + item.qty);

            // Hiển thị tổng tiền của Item (đơn giá * số lượng)
            long totalPrice = item.getTotalPrice();
            String priceFormatted = String.format("%,d", totalPrice).replace(",", ".");
            tvProductPrice.setText(priceFormatted); // SỬA: Bỏ "đ" ở đây

            checkboxItem.setImageResource(
                    item.isSelected ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
            );

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.color.grayLight)
                    .error(R.color.grayLight)
                    .centerCrop()
                    .into(ivProductImage);

            checkboxItem.setOnClickListener(v -> {
                item.isSelected = !item.isSelected;
                checkboxItem.setImageResource(
                        item.isSelected ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
                );
                if (listener != null) {
                    listener.onItemChecked(item, item.isSelected);
                    // 🔥 Cập nhật tổng tiền khi check
                    listener.onTotalChange(calculateTotal());
                }
            });

            layoutViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetail(item);
                }
            });

            layoutDelete.setOnClickListener(v -> {
                if (listener != null) {
                    int currentPosition = getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        listener.onItemDeleted(cartItems.get(currentPosition), currentPosition);
                    }
                }
            });
        }
    }
}
