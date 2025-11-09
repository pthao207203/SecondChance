package com.example.secondchance.ui.shoporder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.model.ShopOrder;
import com.example.secondchance.data.model.ShopOrderProduct;

import java.util.List;

public class ShopOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<ShopOrder> shopOrderList;
    private OnItemClickListener itemClickListener;
    private OnCancelClickListener cancelClickListener;

    private static final int VIEW_TYPE_UNCONFIRMED = 1;
    private static final int VIEW_TYPE_CONFIRMED_FIXED = 2;
    private static final int VIEW_TYPE_CONFIRMED_AUCTION = 3;

    public interface OnItemClickListener {
        void onItemClick(String orderId);
    }

    public interface OnCancelClickListener {
        void onCancelClick(String orderId);
    }

    public ShopOrderAdapter(Context context, List<ShopOrder> shopOrderList,
                            OnItemClickListener itemClickListener,
                            OnCancelClickListener cancelClickListener) {
        this.context = context;
        this.shopOrderList = shopOrderList;
        this.itemClickListener = itemClickListener;
        this.cancelClickListener = cancelClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        ShopOrder order = shopOrderList.get(position);
        if (order == null || order.getType() == null) {
            return VIEW_TYPE_UNCONFIRMED;
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
                view = inflater.inflate(R.layout.item_unconfirm_order, parent, false);
                return new UnconfirmedViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ShopOrder order = shopOrderList.get(position);
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
        return shopOrderList == null ? 0 : shopOrderList.size();
    }

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

        void bind(ShopOrder order) {
            tvPrice.setText(order.getTotalPrice() != null ? order.getTotalPrice() : "0đ"); // Dùng tổng tiền
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");

// Lấy thông tin từ SẢN PHẨM ĐẦU TIÊN
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                // Lấy sản phẩm đầu tiên
                ShopOrderProduct firstProduct = order.getItems().get(0);

                // Gán dữ liệu của sản phẩm đó
                tvTitle.setText(firstProduct.getTitle() != null ? firstProduct.getTitle() : "");
                tvQuantity.setText("x" + firstProduct.getQuantity());
                tvSubtitleFixed.setText(firstProduct.getSubtitle() != null ? firstProduct.getSubtitle() : "");
                imgProduct.setImageResource(firstProduct.getImageRes()); // Ví dụ

                // (Nếu bạn muốn hiển thị "+2 sản phẩm khác", bạn có thể kiểm tra order.getItems().size())

            } else {
                // Trường hợp dự phòng nếu đơn hàng (vì lý do nào đó) không có sản phẩm
                tvTitle.setText("Đơn hàng không có sản phẩm");
                tvQuantity.setText("x0");
                tvSubtitleFixed.setText("Mã đơn: " + order.getId());
            }
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");
            // TODO: Gán ảnh cho imgProduct
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(order.getId()));
            btnCancel.setOnClickListener(v -> cancelClickListener.onCancelClick(order.getId()));
        }
    }

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
            tvAuctionInfo = itemView.findViewById(R.id.tvAuctionInfo);
        }

        void bind(ShopOrder order) {
            tvPrice.setText(order.getTotalPrice() != null ? order.getTotalPrice() : "0đ"); // Dùng tổng tiền
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");

// Lấy thông tin từ SẢN PHẨM ĐẦU TIÊN
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                // Lấy sản phẩm đầu tiên
                ShopOrderProduct firstProduct = order.getItems().get(0);

                // Gán dữ liệu của sản phẩm đó
                tvTitle.setText(firstProduct.getTitle() != null ? firstProduct.getTitle() : "");
                tvQuantity.setText("x" + firstProduct.getQuantity());
                tvSubtitleFixed.setText(firstProduct.getSubtitle() != null ? firstProduct.getSubtitle() : "");
                imgProduct.setImageResource(firstProduct.getImageRes()); // Ví dụ

                // (Nếu bạn muốn hiển thị "+2 sản phẩm khác", bạn có thể kiểm tra order.getItems().size())

            } else {
                // Trường hợp dự phòng nếu đơn hàng (vì lý do nào đó) không có sản phẩm
                tvTitle.setText("Đơn hàng không có sản phẩm");
                tvQuantity.setText("x0");
                tvSubtitleFixed.setText("Mã đơn: " + order.getId());
            }
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");
            tvAuctionInfo.setText(order.getDescription() != null ? order.getDescription() : "");
            // TODO: Gán ảnh cho imgProduct

            itemView.setOnClickListener(v -> itemClickListener.onItemClick(order.getId()));
        }
    }

    class ConfirmedAuctionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvPrice, tvQuantity, tvSubtitleFixed, tvSubtitleDate, tvStatus, tvAuctionInfo;

        ConfirmedAuctionViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvSubtitleFixed = itemView.findViewById(R.id.tvSubtitleFixed);
            tvSubtitleDate = itemView.findViewById(R.id.tvSubtitleDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAuctionInfo = itemView.findViewById(R.id.tvAuctionInfo);
        }

        void bind(ShopOrder order) {
            tvPrice.setText(order.getTotalPrice() != null ? order.getTotalPrice() : "0đ"); // Dùng tổng tiền
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");

// Lấy thông tin từ SẢN PHẨM ĐẦU TIÊN
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                // Lấy sản phẩm đầu tiên
                ShopOrderProduct firstProduct = order.getItems().get(0);

                // Gán dữ liệu của sản phẩm đó
                tvTitle.setText(firstProduct.getTitle() != null ? firstProduct.getTitle() : "");
                tvQuantity.setText("x" + firstProduct.getQuantity());
                tvSubtitleFixed.setText(firstProduct.getSubtitle() != null ? firstProduct.getSubtitle() : "");
                imgProduct.setImageResource(firstProduct.getImageRes()); // Ví dụ

                // (Nếu bạn muốn hiển thị "+2 sản phẩm khác", bạn có thể kiểm tra order.getItems().size())

            } else {
                // Trường hợp dự phòng nếu đơn hàng (vì lý do nào đó) không có sản phẩm
                tvTitle.setText("Đơn hàng không có sản phẩm");
                tvQuantity.setText("x0");
                tvSubtitleFixed.setText("Mã đơn: " + order.getId());
            }
            tvSubtitleDate.setText(order.getDate() != null ? order.getDate() : "");
            tvStatus.setText(order.getStatusText() != null ? order.getStatusText() : "");
            tvAuctionInfo.setText(order.getDescription() != null ? order.getDescription() : "");
            // TODO: Gán ảnh cho imgProduct
            itemView.setOnClickListener(v -> itemClickListener.onItemClick(order.getId()));
        }
    }
}
