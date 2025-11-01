package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class WalletPurchasedHistoryFragment extends Fragment {
  
  private RecyclerView rv;
  private WalletPurchasedHistoryAdapter adapter;
  
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
    adapter = new WalletPurchasedHistoryAdapter(buildDummy());
    rv.setAdapter(adapter);
    rv.setHasFixedSize(true);
    
    adapter.setOnItemClickListener(item -> {
      Bundle args = new Bundle();
      
      NavHostFragment.findNavController(this).navigate(
        R.id.navigation_bought_order_detail,
        args
      );
    });
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
  
  private List<WalletHistoryItem> buildDummy() {
    List<WalletHistoryItem> list = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      list.add(new WalletHistoryItem(
        R.drawable.sample_flower,
        "Giỏ gỗ cắm hoa",
        "Đã thanh toán 17/6/2025",
        "50.000"
      ));
    }
    return list;
  }
}
