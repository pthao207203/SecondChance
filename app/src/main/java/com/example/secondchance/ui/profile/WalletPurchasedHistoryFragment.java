package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.MeApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.WalletPurchasedHistoryResponse;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletPurchasedHistoryFragment extends Fragment {
  
  private RecyclerView rv;
  private WalletPurchasedHistoryAdapter adapter;
  private MeApi meApi;
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_wallet_purchased_history, container, false);
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    rv = view.findViewById(R.id.rvPurchases);
    rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
    adapter = new WalletPurchasedHistoryAdapter(new ArrayList<>());
    rv.setAdapter(adapter);
    rv.setHasFixedSize(true);
    
    adapter.setOnItemClickListener(item -> {
      Bundle args = new Bundle();
      args.putString("orderId", item.orderId);
      NavHostFragment.findNavController(this).navigate(
        R.id.navigation_bought_order_detail,
        args
      );
    });
    meApi = RetrofitProvider.me();
    loadData();
  }
  
  private void loadData() {
    meApi.getPurchasedHistory().enqueue(new Callback<WalletPurchasedHistoryResponse>() {
      @Override
      public void onResponse(@NonNull Call<WalletPurchasedHistoryResponse> call,
                             @NonNull Response<WalletPurchasedHistoryResponse> response) {
        if (!response.isSuccessful() || response.body() == null) {
          Gson gson = new Gson();
          Log.d("WalletPurchasedHistoryFragment", "featured: " + gson.toJson(response.body()));
          Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
          return;
        }
        List<WalletHistoryItem> mapped = mapToUiItems(response.body());
        adapter.setData(mapped);
      }
      
      @Override
      public void onFailure(@NonNull Call<WalletPurchasedHistoryResponse> call, @NonNull Throwable t) {
        Toast.makeText(requireContext(), "Không thể kết nối server", Toast.LENGTH_SHORT).show();
      }
    });
  }
  private List<WalletHistoryItem> mapToUiItems(WalletPurchasedHistoryResponse res) {
    List<WalletHistoryItem> out = new ArrayList<>();
    if (res.data == null || res.data.items == null) return out;
    
    for (WalletPurchasedHistoryResponse.Item it : res.data.items) {
      String title = (it.firstProduct != null && it.firstProduct.name != null && !it.firstProduct.name.isEmpty())
        ? it.firstProduct.name
        : "Đơn hàng " + safeShortId(it.orderId);
      
      String sub = "Đã thanh toán " + formatVnTime(it.time);
      String price = formatVnd(it.amount);
      
      out.add(new WalletHistoryItem(it.firstProduct.image, title, sub, price, it.orderId));
    }
    return out;
  }
  
  private String formatVnd(long amount) {
    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    return nf.format(amount); // ví dụ: "19.020.000"
  }
  
  private String formatVnTime(String iso) {
    try {
      ZonedDateTime zdt = ZonedDateTime.parse(iso) // parse "2025-11-05T14:14:05.996Z"
        .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
      DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      return zdt.format(f);
    } catch (Exception e) {
      return iso; // fallback
    }
  }
  private String safeShortId(String id) {
    if (id == null) return "";
    return id.length() <= 6 ? id : id.substring(id.length() - 6);
  }
  private boolean onToolbarMenuClick(MenuItem item) {
    // TODO: handle click cart/chat/bell
    return true;
  }
  
  private void setupTabs(TabLayout tabLayout) {
    String[] tabs = {"Nạp tiền", "Lịch sử nạp", "Đã mua", "Đã nhận"};
    for (String t : tabs) tabLayout.addTab(tabLayout.newTab().setText(t));
    // chọn "Đã mua"
    TabLayout.Tab tab = tabLayout.getTabAt(2);
    if (tab != null) tab.select();
    
    // Optional: xử lý chuyển tab (nếu có)
    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
      @Override public void onTabSelected(TabLayout.Tab tab) { /* navigate if needed */ }
      @Override public void onTabUnselected(TabLayout.Tab tab) {}
      @Override public void onTabReselected(TabLayout.Tab tab) {}
    });
  }
  
//  private List<WalletHistoryItem> buildDummy() {
//    List<WalletHistoryItem> list = new ArrayList<>();
//    for (int i = 0; i < 4; i++) {
//      list.add(new WalletHistoryItem(
//        R.drawable.sample_flower,
//        "Giỏ gỗ cắm hoa",
//        "Đã thanh toán 17/6/2025",
//        "50.000"
//      ));
//    }
//    return list;
//  }
}
