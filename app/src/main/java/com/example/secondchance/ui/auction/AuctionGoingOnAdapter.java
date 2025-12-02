// ui/auction/AuctionGoingOnAdapter.java
package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemAuctionGoingonCardBinding;

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

        public void bind(AuctionGoingOn auction) {
            // Bind dữ liệu
            binding.productName.setText(auction.getProductName());
            binding.currentPrice.setText(auction.getCurrentPrice());
            binding.quantityText.setText("x" + auction.getQuantity());

            // Luôn hiển thị layout bid info nếu user đã bid
            // Bạn muốn luôn hiển thị khi truy cập trang này (Fragment MyAuctionBidding)
            // Nhưng logic hiển thị ở đây phụ thuộc vào data.
            // Nếu muốn hiển thị mặc định cho item này bất kể có count > 0 hay không thì sửa điều kiện
            // Nhưng thường thì chỉ hiển thị nếu có dữ liệu bid
            if (auction.getUserBidCount() > 0) {
                binding.bidInfoLayout.setVisibility(View.VISIBLE);
                binding.bidStatusText.setText("Đã ra giá lần " + auction.getUserBidCount() + ": " + auction.getUserLastBidPrice());
            } else {
                // Nếu không có thông tin bid (VD chỉ tham gia nhưng chưa bid hoặc lỗi)
                // Tạm thời ẩn, hoặc hiển thị text mặc định?
                // Theo yêu cầu của user: "tôi muốn khi truy cập trang này thì ... là visible"
                // Trang này là trang danh sách "Đang đấu giá" của user.
                // Nên về logic user ĐÃ tham gia thì phải có ít nhất 1 bid hoặc tham gia kiểu gì đó.
                // API trả về myBidAmount, nếu có thì show.

                // Tuy nhiên, để chắc chắn theo yêu cầu, nếu user muốn nó visible,
                // có thể set visible nhưng set text rỗng hoặc text mặc định?
                // Nhưng tốt nhất là chỉ show khi có data hợp lệ.
                // Với fix ở MyAuctionBiddingFragment, nếu có myBidAmount > 0 thì bidCount=1.
                // Nên nó sẽ vào if trên.

                binding.bidInfoLayout.setVisibility(View.GONE);
            }

            // Load ảnh
            Glide.with(binding.getRoot().getContext())
                    .load(auction.getImageUrl())
                    .placeholder(com.example.secondchance.R.drawable.nhan1)
                    .into(binding.auctionImage);

            // Xử lý click
            binding.getRoot().setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                Log.d("AuctionGoingOnAdapter", "bind: " + auction.getProductId());
                bundle.putString("productId", auction.getProductId());

                // Điều hướng sang màn hình AuctionDetailFragment
                try {
                    Navigation.findNavController(v).navigate(
                            R.id.navigation_auction_detail,
                            bundle
                    );
                } catch (Exception e) {
                    Log.e("AuctionGoingOnAdapter", "Navigation error", e);
                    // Fallback nếu cần thiết, nhưng ưu tiên navigation_auction_detail
                }
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
