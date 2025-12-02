// ui/auction/AuctioneerAdapter.java
package com.example.secondchance.ui.auction;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemAuctioneerCardBinding;
import com.example.secondchance.dto.response.AuctionListUserResponse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AuctioneerAdapter extends RecyclerView.Adapter<AuctioneerAdapter.BidViewHolder> {
    
    private final List<AuctionListUserResponse.Bid> data;

    public AuctioneerAdapter(List<AuctionListUserResponse.Bid> data) {
        this.data = data;
    }

    public void updateData(List<AuctionListUserResponse.Bid> newData) {
        this.data.clear();
        if (newData != null) {
            this.data.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAuctioneerCardBinding binding = ItemAuctioneerCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BidViewHolder(binding);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull BidViewHolder h, int position) {
        AuctionListUserResponse.Bid bid = data.get(position);
        
        // Dùng binding thay vì findViewById
        NumberFormat vnd   = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
        NumberFormat plain = NumberFormat.getInstance(new Locale("vi","VN"));
        
        h.binding.auctionDate.setText(com.example.secondchance.util.TimeFmt.isoToVN(bid.createdAt));
        h.binding.price.setText(vnd.format(bid.amount));
        h.binding.userName.setText(bid.byUser.name);
        Glide.with(h.binding.bidAvatar)
          .load(bid.byUser.avatar)
          .circleCrop()
          .into(h.binding.bidAvatar);
        
        long inc = position > 0 ? Math.max(0, bid.amount - data.get(position-1).amount) : 0;
        if (inc > 0) {
            h.binding.priceplus.setText(" (+" + plain.format(inc) + ")");
            h.binding.priceplus.setVisibility(View.VISIBLE);
        } else {
            h.binding.priceplus.setText("");
            h.binding.priceplus.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount(){
        return data == null ? 0 : data.size();
    }


    static class BidViewHolder extends RecyclerView.ViewHolder {
        private final ItemAuctioneerCardBinding binding;

        public BidViewHolder(ItemAuctioneerCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
