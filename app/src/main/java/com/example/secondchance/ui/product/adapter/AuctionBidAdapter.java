package com.example.secondchance.ui.product.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.secondchance.R;
import com.example.secondchance.ui.product.AuctionBid;

import java.util.ArrayList;
import java.util.List;

public class AuctionBidAdapter extends RecyclerView.Adapter<AuctionBidAdapter.BidViewHolder> {

    private List<AuctionBid> bidList;

    public AuctionBidAdapter() {
        this.bidList = new ArrayList<>();
    }

    public void setBidList(List<AuctionBid> bidList) {
        this.bidList = bidList;
        notifyDataSetChanged();
    }

    public void addBid(AuctionBid bid) {
        this.bidList.add(0, bid); // Add to top
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_auction_bid_history, parent, false);
        return new BidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
        AuctionBid bid = bidList.get(position);
        holder.bind(bid);
    }

    @Override
    public int getItemCount() {
        return bidList.size();
    }

    static class BidViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserAvatar;
        TextView tvUserName, tvBidNumber, tvBidAmount, tvIncreaseAmount, tvBidTime;

        public BidViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.img_user_avatar);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvBidNumber = itemView.findViewById(R.id.tv_bid_number);
            tvBidAmount = itemView.findViewById(R.id.tv_bid_amount);
            tvIncreaseAmount = itemView.findViewById(R.id.tv_increase_amount);
            tvBidTime = itemView.findViewById(R.id.tv_bid_time);
        }

        public void bind(AuctionBid bid) {
            tvUserName.setText(bid.getUserName());
            tvBidNumber.setText("Trả giá lần " + bid.getBidNumber());
            tvBidAmount.setText(String.format("%,.0f", bid.getBidAmount()));
            tvIncreaseAmount.setText(String.format("(+%,.0f)", bid.getIncreaseAmount()));
            tvBidTime.setText(bid.getBidTime());

            // TODO: Load avatar with Glide/Picasso
            // Glide.with(itemView.getContext()).load(bid.getUserAvatar()).into(imgUserAvatar);
        }
    }
}