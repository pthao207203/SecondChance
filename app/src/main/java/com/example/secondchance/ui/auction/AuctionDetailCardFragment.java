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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ItemAuctionGoingonCardBinding;
import com.example.secondchance.dto.response.AuctionListUserResponse;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.Locale;

public class AuctionDetailCardFragment extends Fragment {

    private ItemAuctionGoingonCardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.item_auction_goingon_card, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState){
        super.onViewCreated(v, savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
            AuctionDetailFragment.KEY_AUCTION_HEADER, this, (k, b) -> {
                String json = b.getString(AuctionDetailFragment.KEY_AUCTION_JSON);
                if (json == null) return;

                AuctionListUserResponse.Data auction =
                  new Gson().fromJson(json, AuctionListUserResponse.Data.class);
              
                // Ví dụ bind nhanh:
                ImageView img = requireView().findViewById(R.id.auctionImage);
                TextView tvName = requireView().findViewById(R.id.product_name);
                TextView tvQty  = requireView().findViewById(R.id.quantityText);
                TextView tvCur  = requireView().findViewById(R.id.currentPrice);

                // Ẩn phần "đã ra giá lần X" khi hiển thị trong màn hình chi tiết
                View bidInfo = requireView().findViewById(R.id.bid_info_layout);
                if (bidInfo != null) {
                    bidInfo.setVisibility(View.GONE);
                }
              
                Glide.with(this).load(auction.imageUrl).into(img);
              
                tvName.setText(auction.title != null ? auction.title : "");
                tvName.setSingleLine(true);
                tvName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                tvName.setSelected(true);
                tvQty.setText("x" + auction.quantity);
              
                NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi","VN"));
                tvCur.setText(vnd.format(auction.currentPrice));
              
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
        TextView tvH = requireView().findViewById(R.id.hours_text);
        TextView tvM = requireView().findViewById(R.id.minutes_text);
        TextView tvS = requireView().findViewById(R.id.seconds_text);
        if(tvH!=null) tvH.setText(String.format(Locale.getDefault(), "%02d", h));
        if(tvM!=null) tvM.setText(String.format(Locale.getDefault(), "%02d", m));
        if(tvS!=null) tvS.setText(String.format(Locale.getDefault(), "%02d", s));
    }
    @Override public void onDestroyView(){
        super.onDestroyView();
        if (timer!=null) timer.cancel();
    }

}
