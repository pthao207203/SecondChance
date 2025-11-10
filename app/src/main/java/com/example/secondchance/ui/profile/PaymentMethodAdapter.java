package com.example.secondchance.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;

public class PaymentMethodAdapter extends ListAdapter<PaymentMethodItem, PaymentMethodAdapter.PaymentMethodViewHolder> {

    public interface OnPaymentMethodClickListener {
        void onPaymentMethodClick(PaymentMethodItem paymentMethod);
    }

    private OnPaymentMethodClickListener clickListener;

    public void setOnPaymentMethodClickListener(OnPaymentMethodClickListener listener) {
        this.clickListener = listener;
    }

    public PaymentMethodAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false); // SỬA: Dùng đúng layout
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethodItem item = getItem(position);
        holder.bind(item, clickListener);
    }

    static class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBankName;
        private final TextView tvBankDescription;

        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvBankDescription = itemView.findViewById(R.id.tvBankDescription);
        }

        public void bind(PaymentMethodItem item, OnPaymentMethodClickListener listener) {
            tvBankName.setText(item.getDisplayName());
            tvBankDescription.setText(item.getAccountHolderName());

            // Highlight nếu là default
            itemView.setSelected(item.isDefault());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentMethodClick(item);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<PaymentMethodItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PaymentMethodItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull PaymentMethodItem oldItem, @NonNull PaymentMethodItem newItem) {
                    return oldItem.getAccountNumber().equals(newItem.getAccountNumber());
                }

                @Override
                public boolean areContentsTheSame(@NonNull PaymentMethodItem oldItem, @NonNull PaymentMethodItem newItem) {
                    return oldItem.getAccountHolderName().equals(newItem.getAccountHolderName()) &&
                            oldItem.getBankName().equals(newItem.getBankName()) &&
                            oldItem.isDefault() == newItem.isDefault();
                }
            };
}
