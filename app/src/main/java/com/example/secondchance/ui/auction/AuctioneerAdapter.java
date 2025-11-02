// ui/auction/AuctioneerAdapter.java
package com.example.secondchance.ui.auction;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.databinding.ItemAuctioneerCardBinding;

import java.util.List;

public class AuctioneerAdapter extends RecyclerView.Adapter<AuctioneerAdapter.BidViewHolder> {

    private List<Auctioneer> bidList;

    public AuctioneerAdapter(List<Auctioneer> bidList) {
        this.bidList = bidList;
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAuctioneerCardBinding binding = ItemAuctioneerCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BidViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
        Auctioneer bid = bidList.get(position);
        holder.bind(bid);
    }

    @Override
    public int getItemCount() {
        return bidList != null ? bidList.size() : 0;
    }

    static class BidViewHolder extends RecyclerView.ViewHolder {
        private final ItemAuctioneerCardBinding binding;

        public BidViewHolder(ItemAuctioneerCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Auctioneer bid) {
            binding.productName.setText(bid.getBidderName());
            binding.productDate.setText(bid.getBidTime());
            binding.price.setText(bid.getBidAmount());
            binding.priceplus.setText(" " + bid.getPriceDiff());


            binding.priceplus.setVisibility(bid.getPriceDiff().isEmpty() ? View.GONE : View.VISIBLE);

            // Bind avatar
            // SỬA LẠI: Truy cập trực tiếp qua binding.shapeableImageView
            // (Giả sử ID trong XML là android:id="@+id/shapeableImageView")
            //if (binding.shapeableImageView != null) {
             //   binding.shapeableImageView.setImageResource(bid.getAvatarResId());
            //}


            // Tìm TextView "Trả giá lần X"
            // SỬA LẠI: Truy cập trực tiếp qua binding.text1
            // (Giả sử ID trong XML là android:id="@+id/text1")
//            if (binding.text1 != null) {
//                binding.text1.setText(bid.getBidRound());
//            }
        }
    }
}
