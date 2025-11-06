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
import com.example.secondchance.dto.response.WalletReceivedHistoryResponse;
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

public class WalletReceivedHistoryFragment extends Fragment {
  
  private RecyclerView rv;
  private WalletReceivedHistoryAdapter adapter;
  private View progress;
  private MeApi meApi;
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_wallet_received_history, container, false);
  }
  
  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    
    rv = view.findViewById(R.id.rvPurchases);
    rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
    adapter = new WalletReceivedHistoryAdapter(new ArrayList<>());
    rv.setAdapter(adapter);
    rv.setHasFixedSize(true);
    
    adapter.setOnItemClickListener(item -> {
      Gson gson = new Gson();
      Log.d("WalletReceivedHistoryFragment", "item: " + gson.toJson(item));
      Bundle args = new Bundle();
      args.putString("orderId", item.orderId);
      Log.d("WalletReceivedHistoryFragment", "orderId: " + item.orderId);
      NavHostFragment.findNavController(this).navigate(
        R.id.navigation_bought_order_detail,
        args
      );
    });
    meApi = RetrofitProvider.me();
    loadData();
  }
  private void loadData() {
    showLoading(true);
    meApi.getReceivedHistory() // hoặc không có page/pageSize tuỳ API bạn định nghĩa
      .enqueue(new Callback<WalletReceivedHistoryResponse>() {
        @Override
        public void onResponse(@NonNull Call<WalletReceivedHistoryResponse> call,
                               @NonNull Response<WalletReceivedHistoryResponse> response) {
          showLoading(false);
          if (!response.isSuccessful() || response.body() == null) {
            Toast.makeText(requireContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            return;
          }
          Gson gson = new Gson();
          Log.d("WalletReceived", "body: " + gson.toJson(response.body()));
          
          List<WalletHistoryItem> mapped = mapToUiItems(response.body());
          adapter.setData(mapped);
        }
        
        @Override
        public void onFailure(@NonNull Call<WalletReceivedHistoryResponse> call, @NonNull Throwable t) {
          showLoading(false);
          Toast.makeText(requireContext(), "Không thể kết nối server", Toast.LENGTH_SHORT).show();
        }
      });
  }
  private List<WalletHistoryItem> mapToUiItems(WalletReceivedHistoryResponse res) {
    List<WalletHistoryItem> out = new ArrayList<>();
    if (res.data == null || res.data.items == null) return out;
    
    for (WalletReceivedHistoryResponse.Item it : res.data.items) {
      // Title theo type
      String title = titleFromType(it.type);
      
      // Sub: thời gian nhận + mã ref rút gọn
      String refShort = safeShortRef(it.ref);
      String sub = "Nhận lúc " + formatVnTime(it.time) + (refShort.isEmpty() ? "" : ("  •  " + refShort));
      
      // Giá trị: hiển thị +… (đã nhận tiền)
      String price = formatVnd(it.amount);
      
      // Tái dùng WalletHistoryItem; nhét ref vào orderId để tận dụng field có sẵn (hoặc sửa class nếu muốn rõ ràng hơn)
      out.add(new WalletHistoryItem(it.firstProduct.image, title, sub, price, it.orderId));
    }
    return out;
  }
  private String safeShortRef(String ref) {
    if (ref == null || ref.isEmpty()) return "";
    // Lấy 10 ký tự cuối cho gọn; tuỳ bạn format khác
    int n = Math.min(10, ref.length());
    return "#" + ref.substring(ref.length() - n);
  }
  
  private void showLoading(boolean show) {
    if (progress != null) progress.setVisibility(show ? View.VISIBLE : View.GONE);
    if (rv != null) rv.setAlpha(show ? 0.4f : 1f);
  }
  
  private String titleFromType(String type) {
    if (type == null) return "Giao dịch";
    switch (type) {
      case "refund":   return "Hoàn tiền";
      case "topup":    return "Nạp ví";
      case "transfer": return "Nhận chuyển khoản";
      default:         return "Giao dịch";
    }
  }
  
  private int iconFromType(String type) {
    // Bạn thay bằng icon thực tế trong dự án (ic_refund, ic_topup, ic_receive...)
    if ("refund".equals(type))   return R.drawable.sample_flower; // placeholder
    if ("topup".equals(type))    return R.drawable.sample_flower;
    if ("transfer".equals(type)) return R.drawable.sample_flower;
    return R.drawable.sample_flower;
  }
  
  private String formatVnd(long amount) {
    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
    return nf.format(amount);
  }
  
  private String formatVnTime(String iso) {
    try {
      ZonedDateTime zdt = ZonedDateTime.parse(iso)
        .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"));
      DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
      return zdt.format(f);
    } catch (Exception e) {
      return iso;
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
//        "Cá biết bay đã trả lúc 17/6/2025",
//        "50.000"
//      ));
//    }
//    return list;
//  }
}
