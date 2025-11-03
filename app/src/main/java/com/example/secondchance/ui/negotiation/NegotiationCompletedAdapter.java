package com.example.secondchance.ui.negotiation;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.databinding.ItemNegotiationCompletedCardBinding;
import java.util.List;

public class NegotiationCompletedAdapter extends RecyclerView.Adapter<NegotiationCompletedAdapter.ViewHolder> {

    private final List<NegotiationCompleted> completedList;

    public NegotiationCompletedAdapter(List<NegotiationCompleted> completedList) {
        this.completedList = completedList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemNegotiationCompletedCardBinding binding =
                ItemNegotiationCompletedCardBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NegotiationCompleted item = completedList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return completedList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemNegotiationCompletedCardBinding binding;

        public ViewHolder(ItemNegotiationCompletedCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(NegotiationCompleted item) {
            binding.productName.setText(item.getProductName());
            binding.productDate.setText(item.getProductDate());
            binding.negotiationAmount.setText(item.getNegotiationRound());
            binding.tvTitle.setText(item.getTitle());
            binding.tvPrice.setText(item.getPrice());
            binding.tvQuantity.setText(item.getQuantity());
            binding.tvSubtitleFixed.setText(item.getFixedPriceText());
            binding.tvSubtitleDate.setText(item.getCreatedDate());
            binding.tvReply.setText(item.getReplyMessage());
        }
    }
}

