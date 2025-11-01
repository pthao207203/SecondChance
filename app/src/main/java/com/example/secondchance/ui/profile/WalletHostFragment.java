package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.secondchance.R;

public class WalletHostFragment extends Fragment {
  
  private static final String KEY_SELECTED_TAG = "selected_tab_tag";
  private String currentTag = null;
  private View rootView;
  private TextView tabTopup, tabHistory, tabBought, tabReceived;
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      currentTag = savedInstanceState.getString(KEY_SELECTED_TAG, "topup");
    }
    return inflater.inflate(R.layout.fragment_wallet_host, container, false);
  }
  
  @Override
  public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);
    rootView = v;
    
    tabTopup    = v.findViewById(R.id.tab_nap_tien);
    tabHistory  = v.findViewById(R.id.tab_lich_su);
    tabBought   = v.findViewById(R.id.tab_da_mua);
    tabReceived = v.findViewById(R.id.tab_da_nhan);
    
    View.OnClickListener tabClick = tv -> {
      String tag = String.valueOf(tv.getTag());
      switchChildByTag(tag);
      selectTab(tag);
    };
    
    tabTopup.setOnClickListener(tabClick);
    tabHistory.setOnClickListener(tabClick);
    tabBought.setOnClickListener(tabClick);
    tabReceived.setOnClickListener(tabClick);
    
    // Mặc định chọn Topup lần đầu
    if (currentTag == null) {
      switchChildByTag("topup");
      selectTab("topup");
    } else {
      switchChildByTag(currentTag);
    }
  }
  private void selectTab(@NonNull String tag) {
    tabTopup.setSelected("topup".equals(tag));
    tabHistory.setSelected("history".equals(tag));
    tabBought.setSelected("bought".equals(tag));
    tabReceived.setSelected("received".equals(tag));
  }
  
  private void switchChildByTag(String tag) {
    if (tag == null || tag.equals(currentTag)) return;
    
    Fragment child;
//    child = new WalletTopupContentFragment();
    switch (tag) {
      case "history":
        child = new WalletHistoryFragment();
        break;
      case "bought":
        child = new WalletPurchasedHistoryFragment();
        break;
      case "received":
        child = new WalletReceivedHistoryFragment();
        break;
      case "topup":
      default:
        child = new WalletTopupContentFragment();
        tag = "topup";
    }
    
    currentTag = tag;
    
    FragmentTransaction ft = getChildFragmentManager().beginTransaction()
      .setReorderingAllowed(true)
      .setCustomAnimations(
        android.R.anim.fade_in,
        android.R.anim.fade_out,
        android.R.anim.fade_in,
        android.R.anim.fade_out
      )
      .replace(R.id.wallet_child_container, child, tag);
    // Không addToBackStack để nút back không lùi từng tab
    ft.commit();
  }
  
  private void highlightTab(View root, String selectedTag) {
    int[][] idsAndTags = new int[][]{
      {R.id.tab_nap_tien,    "topup".hashCode()},
      {R.id.tab_lich_su,     "history".hashCode()},
      {R.id.tab_da_mua,      "bought".hashCode()},
      {R.id.tab_da_nhan,     "received".hashCode()},
    };
    
    for (int[] pair : idsAndTags) {
      TextView tv = root.findViewById(pair[0]);
      String tag = String.valueOf(tv.getTag());
      boolean selected = tag.equals(selectedTag);
      tv.setBackgroundColor(selected ? 0xFFE1F0F6 : 0x00000000);
      tv.setTextColor(selected ? 0xFF000000 : 0xFF666666);
      tv.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }
  }
  
  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(KEY_SELECTED_TAG, currentTag);
  }
}
