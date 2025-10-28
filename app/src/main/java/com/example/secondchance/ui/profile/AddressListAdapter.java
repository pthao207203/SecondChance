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

public class AddressListAdapter extends ListAdapter<AddressItem, AddressListAdapter.AddressViewHolder> {

    // Interface để xử lý click
    public interface OnAddressClickListener {
        void onAddressClick(AddressItem addressItem);
    }
    private OnAddressClickListener clickListener;

    public void setOnAddressClickListener(OnAddressClickListener listener) {
        this.clickListener = listener;
    }

    // Constructor
    public AddressListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressItem item = getItem(position);
        holder.bind(item, clickListener);
    }

    // ViewHolder
    static class AddressViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvPhone;
        private final TextView tvAddress;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
        }

        public void bind(AddressItem item, OnAddressClickListener listener) {
            tvName.setText(item.getName());
            tvPhone.setText(item.getPhone());
            tvAddress.setText(item.getAddress());

            itemView.setSelected(item.isDefault());
            // Bắt sự kiện click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddressClick(item);
                }
            });
        }
    }

    // DiffUtil để RecyclerView biết item nào thay đổi
    private static final DiffUtil.ItemCallback<AddressItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AddressItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull AddressItem oldItem, @NonNull AddressItem newItem) {
                    return oldItem.getAddress().equals(newItem.getAddress());
                }

                @Override
                public boolean areContentsTheSame(@NonNull AddressItem oldItem, @NonNull AddressItem newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getPhone().equals(newItem.getPhone()) &&
                            oldItem.isDefault() == newItem.isDefault();
                }
            };
}