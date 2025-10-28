package com.example.secondchance.ui.cart;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onItemChecked(CartItem item, boolean isChecked);
        void onItemDeleted(CartItem item, int position);
        void onViewDetail(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemListener listener) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
        }
    }

    public List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    public void selectAll(boolean select) {
        for (CartItem item : cartItems) {
            item.setSelected(select);
        }
        notifyDataSetChanged();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ImageView checkboxItem, ivProductImage;
        private TextView tvProductName, tvProductPrice, tvProductDescription;
        private View layoutDelete, layoutViewDetail;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            checkboxItem = itemView.findViewById(R.id.checkboxItem);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDescription = itemView.findViewById(R.id.tvProductDescription);
            layoutDelete = itemView.findViewById(R.id.layoutDelete);
            layoutViewDetail = itemView.findViewById(R.id.layoutViewDetail);
        }

        public void bind(CartItem item, int position) {
            // Set data
            tvProductName.setText(item.getName());
            tvProductPrice.setText("đ " + String.format("%,d", item.getPrice()));
            tvProductDescription.setText(item.getDescription());

            // Set checkbox state
            checkboxItem.setImageResource(
                    item.isSelected() ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
            );

            // Load image if you have image URL
            // Glide.with(itemView.getContext()).load(item.getImageUrl()).into(ivProductImage);

            // Checkbox click
            checkboxItem.setOnClickListener(v -> {
                item.setSelected(!item.isSelected());
                checkboxItem.setImageResource(
                        item.isSelected() ? R.drawable.ic_checkbox_checked : R.drawable.ic_checkbox_unchecked
                );
                if (listener != null) {
                    listener.onItemChecked(item, item.isSelected());
                }
            });

            // Delete click - Show confirm dialog
            layoutDelete.setOnClickListener(v -> showDeleteConfirmDialog(item, position));

            // View detail click
            layoutViewDetail.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetail(item);
                }
            });
        }

        private void showDeleteConfirmDialog(CartItem item, int position) {
            Dialog dialog = new Dialog(itemView.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_confirm_delete);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Làm tối background
            dialog.getWindow().setDimAmount(0.7f);

            MaterialButton btnConfirmDelete = dialog.findViewById(R.id.btnConfirmDelete);
            MaterialButton btnCancelDelete = dialog.findViewById(R.id.btnCancelDelete);

            btnConfirmDelete.setOnClickListener(v -> {
                dialog.dismiss();
                removeItem(position);
                if (listener != null) {
                    listener.onItemDeleted(item, position);
                }
                showDeleteSuccessDialog();
            });

            btnCancelDelete.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        }

        private void showDeleteSuccessDialog() {
            Dialog dialog = new Dialog(itemView.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_delete_success);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Làm tối background
            dialog.getWindow().setDimAmount(0.7f);

            ImageView btnCloseSuccess = dialog.findViewById(R.id.btnCloseSuccess);
            btnCloseSuccess.setOnClickListener(v -> dialog.dismiss());

            // Auto dismiss after 2 seconds
            itemView.postDelayed(dialog::dismiss, 2000);

            dialog.show();
        }
    }
}