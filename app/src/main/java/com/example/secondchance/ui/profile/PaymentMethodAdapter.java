package com.example.secondchance.ui.profile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;

public class PaymentMethodAdapter extends ListAdapter<PaymentMethodItem, PaymentMethodAdapter.PaymentMethodViewHolder> {

    public interface OnPaymentMethodClickListener {
        void onPaymentMethodClick(PaymentMethodItem paymentMethod);
    }

    public interface OnPaymentMethodDeleteListener {
        void onPaymentMethodDelete(PaymentMethodItem paymentMethod);
    }

    private OnPaymentMethodClickListener clickListener;
    private OnPaymentMethodDeleteListener deleteListener;

    public void setOnPaymentMethodClickListener(OnPaymentMethodClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnPaymentMethodDeleteListener(OnPaymentMethodDeleteListener listener) {
        this.deleteListener = listener;
    }

    public PaymentMethodAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethodItem item = getItem(position);
        holder.bind(item, clickListener, deleteListener);
    }

    static class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvBankName;
        private final TextView tvBankDescription;
        private final ImageView ivBankLogo;
        private final ImageView ivDelete;

        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tvBankName);
            tvBankDescription = itemView.findViewById(R.id.tvBankDescription);
            ivBankLogo = itemView.findViewById(R.id.ivBankLogo);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(PaymentMethodItem item, OnPaymentMethodClickListener listener, OnPaymentMethodDeleteListener deleteListener) {
            tvBankName.setText(item.getBankName() + " " + item.getAccountNumber());
            tvBankDescription.setText(item.getAccountHolderName());

            // Use selector by setting state `selected` for item view
            boolean isSelected = item.isDefault();
            itemView.setSelected(isSelected);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPaymentMethodClick(item);
                }
            });

            ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onPaymentMethodDelete(item);
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
                            oldItem.getAccountNumber().equals(newItem.getAccountNumber()) &&
                            oldItem.isDefault() == newItem.isDefault();
                }
            };
}
