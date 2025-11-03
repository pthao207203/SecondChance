package com.example.secondchance.ui.order.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.model.OrderProduct;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.ArrayList;
import java.util.List;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
    }

    private final List<OrderProduct> productList;
    private final OnSelectionChangedListener listener;

    public OrderProductAdapter(List<OrderProduct> productList, OnSelectionChangedListener listener) {
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
        OrderProduct p = productList.get(position);

        // Thiết lập dữ liệu
        holder.tvTitle.setText(p.getTitle());
        holder.tvDesc.setText(p.getSubtitle());
        holder.tvPrice.setText(p.getPrice());
        holder.tvQuantity.setText("x" + p.getQuantity());
        holder.ivProduct.setImageResource(p.getImageRes());

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(p.isSelected());

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            p.setSelected(isChecked);
            if (listener != null) listener.onSelectionChanged(getSelectedCount());
        });

        holder.itemView.setOnClickListener(v -> {
            holder.cbSelect.setChecked(!holder.cbSelect.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private int getSelectedCount() {
        int count = 0;
        for (OrderProduct p : productList) {
            if (p.isSelected()) count++;
        }
        return count;
    }

    public List<OrderProduct> getSelectedProducts() {
        List<OrderProduct> selected = new ArrayList<>();
        for (OrderProduct p : productList) {
            if (p.isSelected()) selected.add(p);
        }
        return selected;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
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
    }
}