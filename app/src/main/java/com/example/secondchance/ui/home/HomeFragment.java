package com.example.secondchance.ui.home;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.data.repo.HomeRepository;
import com.example.secondchance.ui.card.CardListFragment;
import com.example.secondchance.ui.card.ProductCard;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class  HomeFragment extends Fragment {
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
        try {
          NavHostFragment.findNavController(HomeFragment.this)
            .navigate(R.id.action_home_to_auction);
        } catch (Exception e) {
          Log.e("NAV_TEST", "Navigation error: " + e.getMessage());
        }
      });
    }
    
    RecyclerView rv = view.findViewById(R.id.rvCategories);
    rv.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    rv.setAdapter(new CategoryAdapter());
    catAdapter = (CategoryAdapter) rv.getAdapter();
    
    ImageView ivAuction = view.findViewById(R.id.auctionImage);
    TextView tvName     = view.findViewById(R.id.product_name);
    TextView tvQty      = view.findViewById(R.id.quantityValue);
    TextView tvPrice    = view.findViewById(R.id.currentPrice);
    TextView tvH        = view.findViewById(R.id.hours_text);
    TextView tvM        = view.findViewById(R.id.minutes_text);
    TextView tvS        = view.findViewById(R.id.seconds_text);
    
    HomeRepository repo = new HomeRepository();
    
    // Fetch một lần: vừa bind auction, vừa truyền data xuống CardListFragment
    repo.fetchHome(new HomeRepository.HomeCallback() {
      @Override public void onSuccess(HomeApi.HomeEnvelope.Data data) {
        if (!isAdded()) return;
        
        // ==== 1) Bind featured auction lên Home =====
        HomeApi.FeaturedAuction f = (data != null) ? data.featuredAuction : null;
        if (f != null) {
          Gson gson = new Gson();
          Log.d("HomeFragment", "featured: " + gson.toJson(f));
          
          tvName.setText(f.title != null ? f.title : "");
          tvName.setSingleLine(true);
          tvName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
          tvName.setSelected(true);
          tvQty.setText("x" + f.quantity);
          tvPrice.setText(formatVnd(f.currentPrice));
          Glide.with(ivAuction).load(f.imageUrl).into(ivAuction);
          
          if (featuredTimer != null) { featuredTimer.cancel(); featuredTimer = null; }
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
            @Override public void onFinish() { tvH.setText("00"); tvM.setText("00"); tvS.setText("00"); }
          }.start();
          
          View auctionCard = view.findViewById(R.id.auction_card_home);
          ProductCard product = new ProductCard();
          product.setId(f.id);
          product.setTitle(f.title);
          product.setPrice(formatVnd(f.currentPrice));
          product.setQuantity(f.quantity);
          product.setProductType(ProductCard.ProductType.AUCTION);
          product.setTimeRemaining(calcRemainingFromIso(f.endsAt));
          product.setImageUrl(f.imageUrl);
          Log.d("HomeFragment", "auction: " + gson.toJson(product));
          
          auctionCard.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("productId", product.getId());
            Navigation.findNavController(v)
              .navigate(R.id.action_home_navigation_detail_product, bundle);
          });
        }
        
        // ==== 2) Bind categories (như cũ) =====
        if (data != null && data.categories != null) {
          for (HomeApi.Category c : data.categories) {
            if (c.icon != null) c.icon = c.icon.replace('-', '_');
          }
          catAdapter.submit(data.categories);
        }
        
        // ==== 3) Chuẩn bị danh sách gợi ý để truyền cho CardListFragment =====
        ArrayList<ProductCard> cards = mapSuggestionsToCards(
          data != null && data.suggestions != null ? data.suggestions.items : null
        );
        // Gắn vào container: nếu chưa có fragment con -> tạo bằng newInstance
        Fragment existing = getChildFragmentManager().findFragmentById(R.id.cardListFragmentContainer);
        if (existing == null) {
          CardListFragment frag = CardListFragment.newInstance(cards);
          getChildFragmentManager().beginTransaction()
            .replace(R.id.cardListFragmentContainer, frag, "CardListFragment")
            .commit();
        } else if (existing instanceof CardListFragment) {
          ((CardListFragment) existing).setExternalData(cards);
        }
        
      }
      
      @Override public void onError(String message) {
        if (isAdded())
          Toast.makeText(requireContext(), "Tải Home thất bại: " + message, Toast.LENGTH_SHORT).show();
      }
    });
  }
  
  // ===== helpers =====
  
  private ArrayList<ProductCard> mapSuggestionsToCards(List<HomeApi.SuggestionItem> items) {
    ArrayList<ProductCard> out = new ArrayList<>();
    if (items == null) return out;
    
    for (HomeApi.SuggestionItem it : items) {
      ProductCard.ProductType type;
      if (it.endsInSec > 0) {
        type = ProductCard.ProductType.AUCTION;
      } else {
        String label = it.conditionLabel != null ? it.conditionLabel.toLowerCase(Locale.ROOT) : "";
        if (label.contains("negotiation") || label.contains("offer") || label.contains("deal")
          || label.contains("bargain") || label.contains("thương lượng")) {
          type = ProductCard.ProductType.NEGOTIATION;
        } else {
          type = ProductCard.ProductType.FIXED;
        }
      }
      
      ProductCard pc = new ProductCard();
      pc.setId(it.id);
      pc.setTitle(it.title);
      pc.setQuantity(it.quantity);
      pc.setProductType(type);
      pc.setImageUrl(it.imageUrl);
      
      String priceText = it.currentPrice > 0 ? formatVnd(it.currentPrice) : "0";
      pc.setPrice(priceText);
      
      if (type == ProductCard.ProductType.AUCTION) {
        long sec = Math.max(0, it.endsInSec);
        long h = sec / 3600, m = (sec % 3600) / 60, s = sec % 60;
        pc.setTimeRemaining(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
      }
      out.add(pc);
    }
    return out;
  }
  
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
    return String.format("%,d", v).replace(',', '.');
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
