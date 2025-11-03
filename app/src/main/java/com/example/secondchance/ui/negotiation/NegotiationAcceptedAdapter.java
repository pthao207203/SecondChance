// NegotiationAcceptedAdapter.java
package com.example.secondchance.ui.negotiation;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemNegotiationAcceptedCardBinding;
import com.google.android.material.button.MaterialButton;

public class NegotiationAcceptedAdapter extends ListAdapter<NegotiationAccepted, NegotiationAcceptedAdapter.ViewHolder> {

    public NegotiationAcceptedAdapter() {
        super(new DiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNegotiationAcceptedCardBinding binding = ItemNegotiationAcceptedCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    // ViewHolder KHÔNG STATIC → có thể truy cập adapter
    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNegotiationAcceptedCardBinding binding;

        ViewHolder(ItemNegotiationAcceptedCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NegotiationAccepted item) {
            // Gán dữ liệu
            binding.productName.setText(item.getUserName());
            binding.productDate.setText(item.getDate());
            binding.negotiationAmount.setText(item.getNegotiationText());
            binding.tvTitle.setText(item.getProductTitle());
            binding.tvPrice.setText(item.getPrice());
            binding.tvQuantity.setText("x" + item.getQuantity());
            binding.tvSubtitleFixed.setText("Giá cố định");
            binding.tvSubtitleDate.setText("Đã tạo ngày: " + item.getCreatedDate());
            binding.tvShopName.setText(item.getShopName());
            binding.tvShopDate.setText(item.getReplyDate());
            binding.tvReply.setText(item.getReplyMessage());

            // Xử lý nút thanh toán
            MaterialButton btn = binding.btnPayNow;
            if (item.isPaid()) {
                btn.setText("Đã thanh toán");
                btn.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(btn.getContext(), R.color.grayDay)));
                btn.setTextColor(ContextCompat.getColor(btn.getContext(), R.color.darkerDay));
                btn.setStrokeColor(ColorStateList.valueOf(
                        ContextCompat.getColor(btn.getContext(), R.color.grayDay)));
                btn.setClickable(false);
                btn.setFocusable(false);
            } else {
                btn.setText("Thanh toán ngay");
                btn.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(btn.getContext(), R.color.normalDay)));
                btn.setTextColor(ContextCompat.getColor(btn.getContext(), R.color.whiteDay));
                btn.setStrokeColor(ColorStateList.valueOf(
                        ContextCompat.getColor(btn.getContext(), R.color.normalDay)));
                btn.setClickable(true);
                btn.setFocusable(true);

                btn.setOnClickListener(v -> {
                    item.setPaid(true);
                    // SỬA LỖI: Dùng holder.getAdapterPosition()
                    notifyItemChanged(getAdapterPosition());
                });
            }

            binding.executePendingBindings();
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<NegotiationAccepted> {
        @Override
        public boolean areItemsTheSame(@NonNull NegotiationAccepted oldItem, @NonNull NegotiationAccepted newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull NegotiationAccepted oldItem, @NonNull NegotiationAccepted newItem) {
            return oldItem.isPaid() == newItem.isPaid() &&
                    oldItem.getUserName().equals(newItem.getUserName());
        }
    }
}
