package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemAuctionSuccessCardBinding;
import java.util.List;

public class AuctionSuccessAdapter extends RecyclerView.Adapter<AuctionSuccessAdapter.ViewHolder> {

    private List<AuctionGoingOn> items;

    public AuctionSuccessAdapter(List<AuctionGoingOn> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAuctionSuccessCardBinding binding = ItemAuctionSuccessCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemAuctionSuccessCardBinding binding;

        public ViewHolder(ItemAuctionSuccessCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(AuctionGoingOn item) {
            binding.productName.setText(item.getProductName());
            binding.currentPrice.setText(item.getCurrentPrice());
            binding.quantityText.setText("x" + item.getQuantity());
            
            Glide.with(binding.getRoot().getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.nhan1)
                    .into(binding.auctionImage);
            
            // Click item -> navigate to auction detail
            View.OnClickListener navigateToDetail = v -> {
                Bundle bundle = new Bundle();
                bundle.putString("productId", item.getProductId());
                
                try {
                    Navigation.findNavController(v).navigate(R.id.navigation_auction_detail, bundle);
                } catch (Exception e) {
                    Log.e("AuctionSuccessAdapter", "Navigation error", e);
                }
            };

            // Click view order -> navigate to confirmed orders
            View.OnClickListener navigateToOrder = v -> {
                Bundle bundle = new Bundle();
                bundle.putInt("selectedTab", 0); // 0 is ConfirmationFragment
                try {
                    Navigation.findNavController(v).navigate(R.id.navigation_order, bundle);
                } catch (Exception e) {
                    Log.e("AuctionSuccessAdapter", "Navigation error", e);
                }
            };
            
            binding.tvViewOrder.setOnClickListener(navigateToOrder);
            binding.getRoot().setOnClickListener(navigateToDetail);
        }
    }
}
