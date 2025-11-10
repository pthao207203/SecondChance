package com.example.secondchance.ui.order.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.model.OrderItem;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private final List<OrderItem> productList;
    private final OnSelectionChangedListener listener;

    private final Set<String> selectedProductIds = new HashSet<>();

    public OrderProductAdapter(List<OrderItem> productList, OnSelectionChangedListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_refund_request_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem p = productList.get(position);
        holder.bind(p);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private int getSelectedCount() {
        return selectedProductIds.size();
    }

    public List<OrderItem> getSelectedProducts() {
        List<OrderItem> selected = new ArrayList<>();
        for (OrderItem p : productList) {

            if (selectedProductIds.contains(p.productId)) {
                selected.add(p);
            }
        }
        return selected;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final CheckBox cbSelect;
        final ShapeableImageView ivProduct;
        final TextView tvTitle, tvDesc, tvPrice, tvQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cbSelect = itemView.findViewById(R.id.cbSelect);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }

        void bind(final OrderItem p) {
            tvTitle.setText(p.getName());

            tvDesc.setText("Số lượng: " + p.getQuantity());

            String formattedPrice = String.format(Locale.GERMAN, "%,dđ", p.getPrice());
            tvPrice.setText(formattedPrice);

            tvQuantity.setText("x" + p.getQuantity());

            Glide.with(itemView.getContext())
                    .load(p.getImageUrl())
                    .into(ivProduct);

            cbSelect.setOnCheckedChangeListener(null);
            cbSelect.setChecked(selectedProductIds.contains(p.productId));

            cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedProductIds.add(p.productId);
                } else {
                    selectedProductIds.remove(p.productId);
                }
                if (listener != null) listener.onSelectionChanged(getSelectedCount());
            });

            itemView.setOnClickListener(v -> {
                cbSelect.setChecked(cbSelect.isChecked());
            });
        }
    }
}
