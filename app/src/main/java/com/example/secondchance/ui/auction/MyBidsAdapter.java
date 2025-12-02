package com.example.secondchance.ui.auction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.dto.response.AuctionListUserResponse;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MyBidsAdapter extends RecyclerView.Adapter<MyBidsAdapter.MyBidViewHolder> {

    private final List<AuctionListUserResponse.Bid> myBids;
    private final List<AuctionListUserResponse.Bid> allBidsSorted;
    private final DateTimeFormatter VN_DATETIME = DateTimeFormatter.ofPattern("HH:mm:ss, dd/MM/yyyy").withLocale(new Locale("vi", "VN"));

    public MyBidsAdapter(List<AuctionListUserResponse.Bid> myBids, List<AuctionListUserResponse.Bid> allBids) {
        this.myBids = myBids != null ? myBids : Collections.emptyList();
        
        // Create a sorted copy of allBids (Oldest First) for logic calculation
        List<AuctionListUserResponse.Bid> sorted = new ArrayList<>();
        if (allBids != null) {
            sorted.addAll(allBids);
        }
        Collections.sort(sorted, (o1, o2) -> o1.createdAt.compareTo(o2.createdAt));
        this.allBidsSorted = sorted;
    }

    @NonNull
    @Override
    public MyBidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_bid, parent, false);
        return new MyBidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyBidViewHolder holder, int position) {
        AuctionListUserResponse.Bid bid = myBids.get(position);
        // Assuming myBids is sorted Newest First (Descending).
        // The index label "Lần X" should be (Total - position).
        // e.g. 3 bids. Pos 0 is Bid 3 (Lần 3). Pos 2 is Bid 1 (Lần 1).
        int labelIndex = myBids.size() - position;
        
        holder.bind(bid, labelIndex, allBidsSorted);
    }

    @Override
    public int getItemCount() {
        return myBids.size();
    }

    class MyBidViewHolder extends RecyclerView.ViewHolder {
        TextView tvBidIndex, tvBidAmount, tvBidIncrease, tvBidTime;

        MyBidViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBidIndex = itemView.findViewById(R.id.tvBidIndex);
            tvBidAmount = itemView.findViewById(R.id.tvBidAmount);
            tvBidIncrease = itemView.findViewById(R.id.tvBidIncrease);
            tvBidTime = itemView.findViewById(R.id.tvBidTime);
        }

        void bind(AuctionListUserResponse.Bid bid, int index, List<AuctionListUserResponse.Bid> allBidsSorted) {
            tvBidIndex.setText("Lần " + index + ": ");
            tvBidAmount.setText(formatVnd(bid.amount));
            tvBidTime.setText(formatIsoToVN(bid.createdAt));

            long increase = findIncrease(bid, allBidsSorted);
            if (increase > 0) {
                tvBidIncrease.setText(" (+" + formatPlain(increase) + ")");
                tvBidIncrease.setVisibility(View.VISIBLE);
            } else {
                tvBidIncrease.setVisibility(View.GONE);
            }
        }

        private long findIncrease(AuctionListUserResponse.Bid currentBid, List<AuctionListUserResponse.Bid> allBidsSorted) {
            int currentIndex = -1;
            // allBidsSorted is Oldest First
            for (int i = 0; i < allBidsSorted.size(); i++) {
                if (allBidsSorted.get(i).createdAt.equals(currentBid.createdAt) && allBidsSorted.get(i).amount == currentBid.amount) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex > 0) {
                // Compare with previous (Older) bid
                return Math.max(0, currentBid.amount - allBidsSorted.get(currentIndex - 1).amount);
            }
            return 0;
        }

        private String formatVnd(long amount) {
            return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
        }

        private String formatPlain(long amount) {
            return NumberFormat.getInstance(new Locale("vi", "VN")).format(amount);
        }

        private String formatIsoToVN(String iso) {
            try {
                return java.time.Instant.parse(iso)
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(VN_DATETIME);
            } catch (Exception e) {
                return iso;
            }
        }
    }
}
