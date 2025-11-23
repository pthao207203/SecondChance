// ui/auction/AuctionGoingOnAdapter.java
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemAuctionGoingonCardBinding;
import com.example.secondchance.ui.card.ProductCard;

import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AuctionGoingOnAdapter extends RecyclerView.Adapter<AuctionGoingOnAdapter.AuctionViewHolder> {

    private List<AuctionGoingOn> auctionList;

    public AuctionGoingOnAdapter(List<AuctionGoingOn> auctionList) {
        this.auctionList = auctionList;
    }

    @NonNull
    @Override
    public AuctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAuctionGoingonCardBinding binding = ItemAuctionGoingonCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AuctionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AuctionViewHolder holder, int position) {
        AuctionGoingOn item = auctionList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return auctionList != null ? auctionList.size() : 0;
    }

    static class AuctionViewHolder extends RecyclerView.ViewHolder {
        private final ItemAuctionGoingonCardBinding binding;
        private CountDownTimer countDownTimer;

        public AuctionViewHolder(ItemAuctionGoingonCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // ... bên trong class AuctionViewHolder ...

        public void bind(AuctionGoingOn auction) {
            // Bind dữ liệu
            binding.productName.setText(auction.getProductName());
            binding.currentPrice.setText(auction.getCurrentPrice());

            // SỬA LẠI: Truy cập trực tiếp qua Data Binding
            // Bỏ hết 2 dòng findViewById(android.R.id...) gây crash
            binding.quantityText.setText("x" + auction.getQuantity());

            // Load ảnh (dùng Glide hoặc Picasso)
            Glide.with(binding.getRoot().getContext())
                    .load(auction.getImageUrl())
                    .placeholder(com.example.secondchance.R.drawable.nhan1)
                    .into(binding.auctionImage);

            binding.getRoot().setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                Log.d("AuctionGoingOnAdapter", "bind: " + auction.getProductId());
                bundle.putSerializable("productId", auction.getProductId());

                Navigation.findNavController(v).navigate(
                        R.id.action_auction_to_detail_product,
                        bundle
                );
            });

            // Hủy timer cũ nếu có
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            // Tính thời gian còn lại
            long millisLeft = auction.getEndTimeMillis() - System.currentTimeMillis();
            if (millisLeft > 0) {
                startCountdown(millisLeft);
            } else {
                binding.hoursText.setText("00");
                binding.minutesText.setText("00");
                binding.secondsText.setText("00");
            }
        }

        private void startCountdown(long millisInFuture) {
            countDownTimer = new CountDownTimer(millisInFuture, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                    binding.hoursText.setText(String.format("%02d", hours));
                    binding.minutesText.setText(String.format("%02d", minutes));
                    binding.secondsText.setText(String.format("%02d", seconds));
                }

                @Override
                public void onFinish() {
                    binding.hoursText.setText("00");
                    binding.minutesText.setText("00");
                    binding.secondsText.setText("00");
                }
            }.start();
        }

        // Hủy timer khi View bị recycle
        public void cancelTimer() {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull AuctionViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cancelTimer();
    }
}
