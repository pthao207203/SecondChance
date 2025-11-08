package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil; // QUAN TRỌNG: Dùng cái này
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.secondchance.R; // Đảm bảo bạn import R

// 1. Import class Binding của bạn (từ tên file item_auction_goingon_card.xml)
import com.example.secondchance.databinding.ItemAuctionGoingonCardBinding;
import com.example.secondchance.dto.response.AuctionListUserResponse;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.Locale;

public class AuctionDetailCardFragment extends Fragment { // 2. Tên class MỚI

    private ItemAuctionGoingonCardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 3. Dùng DataBindingUtil.inflate vì file XML của bạn có thẻ <layout>
        binding = DataBindingUtil.inflate(inflater, R.layout.item_auction_goingon_card, container, false);

        // 4. (Tùy chọn) Gán dữ liệu (DataBinding)
        // Ví dụ: Lấy ID sản phẩm từ argument, gọi ViewModel, rồi gán
        // AuctionGoingOn data = viewModel.getAuctionDetails(auctionId);
        // binding.setRequest(data);
        // binding.setHasReply(false);

        // 5. Trả về root của layout đã binding
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState){
        super.onViewCreated(v, savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
            AuctionDetailFragment.KEY_AUCTION_HEADER, this, (k, b) -> {
                AuctionListUserResponse.Data auction =
                  new Gson().fromJson(b.getString(AuctionDetailFragment.KEY_AUCTION_JSON),
                    AuctionListUserResponse.Data.class);
              
                // Ví dụ bind nhanh:
                ImageView img = requireView().findViewById(R.id.auctionImage);
                TextView tvName = requireView().findViewById(R.id.product_name);
                TextView tvQty  = requireView().findViewById(R.id.quantityText);
                TextView tvCur  = requireView().findViewById(R.id.currentPrice);
              
                Glide.with(this).load(auction.imageUrl)
                    .into(img);
              
                tvName.setText(auction.title != null ? auction.title : "");
                tvName.setSingleLine(true);
                tvName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                tvName.setSelected(true);
                tvQty.setText("x" + auction.quantity);
              
                NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
                tvCur.setText(vnd.format(auction.currentPrice));
              
                // Countdown: tách giờ/phút/giây vào 3 TextView hours_text, minutes_text, seconds_text
                startCountdown(auction.endsAt);
          }
        );
    }
    private CountDownTimer timer;
    private void startCountdown(String endsAtIso) {
        if (timer != null) timer.cancel();
        long end = 0;
        try { end = java.time.Instant.parse(endsAtIso).toEpochMilli(); } catch (Exception ignore) {}
        long remain = end - System.currentTimeMillis();
        if (remain <= 0) { setHMS(0,0,0); return; }
        
        timer = new CountDownTimer(remain, 1000) {
            @Override public void onTick(long ms) {
                long s = ms/1000;
                long h = s/3600; long m = (s%3600)/60; long sec = s%60;
                setHMS(h, m, sec);
            }
            @Override public void onFinish() { setHMS(0,0,0); }
        };
        timer.start();
    }
    private void setHMS(long h,long m,long s){
        ((TextView) requireView().findViewById(R.id.hours_text)).setText(String.format(Locale.getDefault(), "%02d", h));
        ((TextView) requireView().findViewById(R.id.minutes_text)).setText(String.format(Locale.getDefault(), "%02d", m));
        ((TextView) requireView().findViewById(R.id.seconds_text)).setText(String.format(Locale.getDefault(), "%02d", s));
    }
    @Override public void onDestroyView(){
        super.onDestroyView();
        if (timer!=null) timer.cancel();
    }

}
