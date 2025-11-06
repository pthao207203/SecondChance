package com.example.secondchance.ui.home;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.repo.HomeRepository;
import com.example.secondchance.ui.card.ProductCard;
import com.google.android.material.card.MaterialCardView; // 👈 THÊM IMPORT NÀY
import com.google.gson.Gson;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
  private CountDownTimer featuredTimer;
  private CategoryAdapter catAdapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    View btnSeeAll = view.findViewById(R.id.tvSeeAllAuction);
    if (btnSeeAll != null) {
      btnSeeAll.setOnClickListener(v -> {
        Log.d("NAV_TEST", "Clicked Xem tat ca");
        try {
          NavHostFragment.findNavController(HomeFragment.this)
            .navigate(R.id.action_home_to_auction);
        } catch (Exception e) {
          Log.e("NAV_TEST", "Navigation error: " + e.getMessage());
        }
      });
    }
    
    RecyclerView rv = view.findViewById(R.id.rvCategories);
    rv.setLayoutManager(new LinearLayoutManager(requireContext(),
      LinearLayoutManager.HORIZONTAL, false));
    rv.setAdapter(new CategoryAdapter());  // hoặc giữ vào field
    catAdapter = (CategoryAdapter) rv.getAdapter();
    
    HomeRepository repo = new HomeRepository();
    repo.fetchHome(new HomeRepository.HomeCallback() {
      @Override public void onSuccess(HomeApi.HomeEnvelope.Data data) {
        if (!isAdded()) return;
        // ... phần featuredAuction như bạn đã có ...
        if (data != null && data.categories != null) {
          // chuẩn hóa icon một lần nếu cần
          for (HomeApi.Category c : data.categories) {
            if (c.icon != null) c.icon = c.icon.replace('-', '_');
          }
          catAdapter.submit(data.categories);
        }
      }
      @Override public void onError(String message) { /* toast ... */ }
    });
    ImageView ivAuction = view.findViewById(R.id.auctionImage);
    TextView tvName     = view.findViewById(R.id.product_name);
    TextView tvQty      = view.findViewById(R.id.quantityValue);
    TextView tvPrice    = view.findViewById(R.id.currentPrice);
    TextView tvH        = view.findViewById(R.id.hours_text);
    TextView tvM        = view.findViewById(R.id.minutes_text);
    TextView tvS        = view.findViewById(R.id.seconds_text);
    
    // giữ biến timer để cancel khi destroy
    final CountDownTimer[] timerRef = new CountDownTimer[1];
    
    View auctionCard = view.findViewById(R.id.auction_card_home);
    
    repo.fetchHome(new HomeRepository.HomeCallback() {
      @Override public void onSuccess(HomeApi.HomeEnvelope.Data data) {
        if (!isAdded()) return;
        HomeApi.FeaturedAuction f = (data != null) ? data.featuredAuction : null;
        if (f == null) return;
        // Chuyển đổi object f thành chuỗi JSON để log cho dễ đọc
        Gson gson = new Gson();
//        String jsonData = gson.toJson(f);
//        Log.d("HomeFragment", "Dữ liệu nhận được từ backend: " + jsonData);
        
        // 1) Bind UI ngay trên Home
        tvName.setText(f.title != null ? f.title : "");
        tvName.setSingleLine(true);
        tvName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        tvName.setSelected(true);
        tvQty.setText("x" + f.quantity);
        tvPrice.setText(formatVnd(f.currentPrice));
        
        Glide.with(ivAuction)
          .load(f.imageUrl)
          .into(ivAuction);
        
        // 2) Countdown
        if (featuredTimer != null) {
          featuredTimer.cancel();
          featuredTimer = null;
        }
        long millis = millisUntil(f.endsAt);
        featuredTimer = new CountDownTimer(Math.max(0, millis), 1000L) {
          @Override public void onTick(long ms) {
            long h = TimeUnit.MILLISECONDS.toHours(ms);
            long m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
            long s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
            tvH.setText(String.format(Locale.getDefault(), "%02d", h));
            tvM.setText(String.format(Locale.getDefault(), "%02d", m));
            tvS.setText(String.format(Locale.getDefault(), "%02d", s));
          }
          @Override public void onFinish() {
            tvH.setText("00"); tvM.setText("00"); tvS.setText("00");
          }
        }.start();
        
        // 3) Click -> đi tới chi tiết (bundle ProductCard như bạn đang làm)
        ProductCard product = new ProductCard();
        product.setTitle(f.title);
        product.setPrice(formatVnd(f.currentPrice));
        product.setQuantity(f.quantity);
        product.setProductType(ProductCard.ProductType.AUCTION);
        product.setTimeRemaining(calcRemainingFromIso(f.endsAt));
        product.setImageUrl(f.imageUrl);
        
        auctionCard.setOnClickListener(v -> {
          Bundle bundle = new Bundle();
          bundle.putSerializable("product", product);
          Navigation.findNavController(v)
            .navigate(R.id.action_home_navigation_detail_product, bundle);
        });
      }
      
      @Override public void onError(String message) {
        if (isAdded())
          Toast.makeText(requireContext(), "Tải Home thất bại: " + message, Toast.LENGTH_SHORT).show();
      }
    });
  }
  
  
  // ===== helpers trong HomeFragment =====
  
  private long millisUntil(String iso) {
    if (iso == null) return 0;
    try {
      SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
      in.setTimeZone(TimeZone.getTimeZone("UTC"));
      long end = in.parse(iso).getTime();
      return end - System.currentTimeMillis();
    } catch (ParseException e) {
      return 0;
    }
  }
  private String formatVnd(long v) {
    return "₫" + String.format("%,d", v).replace(',', '.');
  }
  
  private String calcRemainingFromIso(String iso) {
    if (iso == null) return "00:00:00";
    try {
      SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
      in.setTimeZone(TimeZone.getTimeZone("UTC"));
      long end = in.parse(iso).getTime();
      long now = System.currentTimeMillis();
      long diff = Math.max(0, end - now);
      long h = TimeUnit.MILLISECONDS.toHours(diff);
      long m = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
      long s = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
      return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
    } catch (ParseException e) {
      return "00:00:00";
    }
  }
  
  @Override
  public void onPause() {
    super.onPause();
    if (featuredTimer != null) {
      featuredTimer.cancel();
      featuredTimer = null;
    }
  }
  
  @Override
  public void onDestroyView() {
    super.onDestroyView();
    if (featuredTimer != null) {
      featuredTimer.cancel();
      featuredTimer = null;
    }
  }
}