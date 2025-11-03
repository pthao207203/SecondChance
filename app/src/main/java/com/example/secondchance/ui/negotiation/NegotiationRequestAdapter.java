// ui/negotiation/NegotiationRequestAdapter.java
package com.example.secondchance.ui.negotiation;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.databinding.ItemNegotiationRequestCardBinding;

public class NegotiationRequestAdapter extends ListAdapter<NegotiationRequest, NegotiationRequestAdapter.ViewHolder> {

    public NegotiationRequestAdapter() {
        super(new DiffCallback());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNegotiationRequestCardBinding binding = ItemNegotiationRequestCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNegotiationRequestCardBinding binding;

        ViewHolder(ItemNegotiationRequestCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NegotiationRequest request) {
            binding.setRequest(request);
            binding.setHasReply(request.isHasReply());
            binding.executePendingBindings();
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<NegotiationRequest> {
        @Override
        public boolean areItemsTheSame(@NonNull NegotiationRequest oldItem, @NonNull NegotiationRequest newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull NegotiationRequest oldItem, @NonNull NegotiationRequest newItem) {
            return oldItem.getUserName().equals(newItem.getUserName()) &&
                    oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.getProductTitle().equals(newItem.getProductTitle()) &&
                    oldItem.isHasReply() == newItem.isHasReply();
        }
    }
}
