package com.example.secondchance.ui.order.adapter; // Hoặc package bạn muốn đặt Adapter

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// QUAN TRỌNG: Import đúng lớp Order từ data.model
import com.example.secondchance.data.model.Order;
import com.example.secondchance.R;

import java.util.List; // Sửa thành List thay vì ArrayList

public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnItemClickListener itemClickListener;
    private OnCancelClickListener cancelClickListener;

    // Các hằng số định nghĩa view type
    private static final int VIEW_TYPE_UNCONFIRMED = 1;
    private static final int VIEW_TYPE_CONFIRMED_FIXED = 2;
    private static final int VIEW_TYPE_CONFIRMED_AUCTION = 3;

    // --- Interfaces cho click listeners ---
    public interface OnItemClickListener {
        void onItemClick(String orderId);
    }
    public interface OnCancelClickListener {
        void onCancelClick(String orderId);
    }

    // Constructor đã sửa: Nhận List<Order> và 2 listeners
    public OrderAdapter(Context context, List<Order> orderList, OnItemClickListener itemClickListener, OnCancelClickListener cancelClickListener) {
        this.context = context;
        this.orderList = orderList;
        this.itemClickListener = itemClickListener;
        this.cancelClickListener = cancelClickListener;
    }

    // Bước A: Xác định view type dựa trên dữ liệu
    @Override
    public int getItemViewType(int position) {
        Order order = orderList.get(position);
        if (order == null || order.getType() == null) {
            return VIEW_TYPE_UNCONFIRMED; // Mặc định an toàn
        }
        switch (order.getType()) {
            case UNCONFIRMED:
                return VIEW_TYPE_UNCONFIRMED;
            case CONFIRMED_FIXED:
                return VIEW_TYPE_CONFIRMED_FIXED;
            case CONFIRMED_AUCTION:
                return VIEW_TYPE_CONFIRMED_AUCTION;
            default:
                return VIEW_TYPE_UNCONFIRMED;
        }
    }

    // Bước B: Tạo ViewHolder tương ứng với view type
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        switch (viewType) {
            case VIEW_TYPE_UNCONFIRMED:
                view = inflater.inflate(R.layout.item_unconfirm_order, parent, false);
                return new UnconfirmedViewHolder(view);
            case VIEW_TYPE_CONFIRMED_FIXED:
                view = inflater.inflate(R.layout.item_confirm_fixed_order, parent, false);
                return new ConfirmedFixedViewHolder(view);
            case VIEW_TYPE_CONFIRMED_AUCTION:
                view = inflater.inflate(R.layout.item_confirm_auction_order, parent, false);
                return new ConfirmedAuctionViewHolder(view);
            default:
                // Trường hợp mặc định
                view = inflater.inflate(R.layout.item_unconfirm_order, parent, false);
                return new UnconfirmedViewHolder(view);
        }
    }

    // Bước C: Gán dữ liệu vào ViewHolder tương ứng
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Order order = orderList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_UNCONFIRMED:
                ((UnconfirmedViewHolder) holder).bind(order);
                break;
            case VIEW_TYPE_CONFIRMED_FIXED:
                ((ConfirmedFixedViewHolder) holder).bind(order);
                break;
            case VIEW_TYPE_CONFIRMED_AUCTION:
                ((ConfirmedAuctionViewHolder) holder).bind(order);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList == null ? 0 : orderList.size();
    }

    // --- ViewHolder cho item_unconfirm_order.xml ---
    class UnconfirmedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgDot;
        TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus;
        Button btnCancel;

        UnconfirmedViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgDot = itemView.findViewById(R.id.imgDot);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtitleFixed = itemView.findViewById(R.id.tvSubtitleFixed);
            tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }

        void bind(Order order) {
            tvTitle.setText(order.getTitle() != null ? order.getTitle() : "");
            tvPrice.setText(order.getPrice() != null ? order.getPrice() : "0 đ");
            tvQuantity.setText(order.getQuantity() != null ? order.getQuantity() : "");
            tvSubtitleFixed.setText(order.getSubtitle() != null ? order.getSubtitle() : "");
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");
            // TODO: Gán ảnh cho imgProduct (ví dụ: dùng Glide hoặc Picasso)
            // Glide.with(context).load(order.getImageUrl()).into(imgProduct);

            itemView.setOnClickListener(v -> itemClickListener.onItemClick(order.getId()));
            btnCancel.setOnClickListener(v -> cancelClickListener.onCancelClick(order.getId()));
        }
    }

    // --- ViewHolder cho item_confirm_fixed_order.xml ---
    class ConfirmedFixedViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus, tvAuctionInfo;

        ConfirmedFixedViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtitleFixed = itemView.findViewById(R.id.tvSubtitleFixed);
            tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAuctionInfo = itemView.findViewById(R.id.tvAuctionInfo); // TextView mô tả
        }

        void bind(Order order) {
            tvTitle.setText(order.getTitle() != null ? order.getTitle() : "");
            tvPrice.setText(order.getPrice() != null ? order.getPrice() : "0 đ");
            tvQuantity.setText(order.getQuantity() != null ? order.getQuantity() : "");
            tvSubtitleFixed.setText(order.getSubtitle() != null ? order.getSubtitle() : "");
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");
            tvAuctionInfo.setText(order.getDescription() != null ? order.getDescription() : "");
            // TODO: Gán ảnh cho imgProduct

            itemView.setOnClickListener(v -> itemClickListener.onItemClick(order.getId()));
        }
    }

    // --- ViewHolder cho item_confirm_auction_order.xml ---
    class ConfirmedAuctionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus, tvAuctionInfo;

        ConfirmedAuctionViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtitleFixed = itemView.findViewById(R.id.tvSubtitleFixed); // TextView "Đấu giá"
            tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAuctionInfo = itemView.findViewById(R.id.tvAuctionInfo); // TextView mô tả
        }

        void bind(Order order) {
            tvTitle.setText(order.getTitle() != null ? order.getTitle() : "");
            tvPrice.setText(order.getPrice() != null ? order.getPrice() : "0 đ");
            tvQuantity.setText(order.getQuantity() != null ? order.getQuantity() : "");
            tvSubtitleFixed.setText(order.getSubtitle() != null ? order.getSubtitle() : ""); // Hiển thị "Đấu giá"
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");
            tvAuctionInfo.setText(order.getDescription() != null ? order.getDescription() : "");
            // TODO: Gán ảnh cho imgProduct

            itemView.setOnClickListener(v -> itemClickListener.onItemClick(order.getId()));
        }
    }
}