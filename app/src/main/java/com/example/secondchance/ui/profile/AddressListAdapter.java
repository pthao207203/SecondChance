package com.example.secondchance.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.data.model.LabelProvider;

public class AddressListAdapter extends ListAdapter<AddressItem, AddressListAdapter.AddressViewHolder> {

    // Interface để xử lý click
    public interface OnAddressClickListener {
        void onAddressClick(AddressItem addressItem);
    }
    
    public interface OnDeleteClickListener {
        void onDeleteClick(AddressItem addressItem);
    }
    
    private OnAddressClickListener clickListener;
    private OnDeleteClickListener deleteListener;

    public void setOnAddressClickListener(OnAddressClickListener listener) {
        this.clickListener = listener;
    }
    
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
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
        holder.bind(item, clickListener, deleteListener);
    }

    // ViewHolder
    static class AddressViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLabel;
        private final ImageView ivLabel; // Changed to match XML
        private final TextView tvName;
        private final TextView tvPhone;
        private final TextView tvAddress;
        private final ImageView ivDelete;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvLabel);
            ivLabel = itemView.findViewById(R.id.ivLabel); // Changed to match XML
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }

        public void bind(AddressItem item, OnAddressClickListener listener, OnDeleteClickListener deleteListener) {
            // Bind label and icon
            String labelApiValue = LabelProvider.getApiValue(item.getLabel()); // Normalize to API value for icon check
            // Or if item.getLabel() stores "Nhà" / "Công ty", convert back to check
            
            String displayLabel = item.getLabel();
            if (tvLabel != null) {
                tvLabel.setText(displayLabel);
            }
            
            if (ivLabel != null) {
                // Check normalized value or display value
                if (displayLabel != null && (displayLabel.equalsIgnoreCase("house") || displayLabel.equalsIgnoreCase("Nhà"))) {
                    ivLabel.setImageResource(R.drawable.ic_house);
                } else {
                     ivLabel.setImageResource(R.drawable.ic_company);
                }
            }

            tvName.setText(item.getName());
            tvPhone.setText(item.getPhone());
            tvAddress.setText(item.getAddress());

            itemView.setSelected(item.isDefault());
            
            // Bắt sự kiện click item
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddressClick(item);
                }
            });
            
            // Bắt sự kiện click delete
            if (ivDelete != null) {
                ivDelete.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteClick(item);
                    }
                });
            }
        }
    }

    // DiffUtil để RecyclerView biết item nào thay đổi
    private static final DiffUtil.ItemCallback<AddressItem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AddressItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull AddressItem oldItem, @NonNull AddressItem newItem) {
                    // Use ID if available, otherwise fallback to address content or reference
                    if (oldItem.getId() != null && newItem.getId() != null) {
                        return oldItem.getId().equals(newItem.getId());
                    }
                    return oldItem.getAddress().equals(newItem.getAddress());
                }

                @Override
                public boolean areContentsTheSame(@NonNull AddressItem oldItem, @NonNull AddressItem newItem) {
                    return oldItem.getName().equals(newItem.getName()) &&
                            oldItem.getPhone().equals(newItem.getPhone()) &&
                            oldItem.isDefault() == newItem.isDefault() &&
                             oldItem.getAddress().equals(newItem.getAddress()) &&
                             (oldItem.getLabel() == null ? newItem.getLabel() == null : oldItem.getLabel().equals(newItem.getLabel()));
                }
            };
}
