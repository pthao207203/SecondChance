package com.example.secondchance.ui.order;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.secondchance.MainActivity;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ActivityStatusOrdersBinding; // ✅ ĐÚNG VỚI TÊN LAYOUT
import com.example.secondchance.viewmodel.SharedViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.lifecycle.ViewModelProvider;

public class StatusOrderFragment extends Fragment {
    
    private static final String TAG_ACTIVITY = "DBG_ORDER_FRAGMENT";
    private static final String TAG_PAGER = "DBG_ORDER_PAGER";
    
    private ActivityStatusOrdersBinding binding;   // ✅ ĐÚNG
    private SharedViewModel sharedViewModel;
    
    private final String[] tabTitles = new String[]{"Xác nhận", "Đang giao", "Đã mua", "Đã hủy", "Hoàn trả"};
    private int initialSelectedTab = 0;
    
    private TabLayoutMediator mediator;
    private TabLayout tabLayoutRef;
    private ViewPager2 pagerRef;
    private ViewPager2.OnPageChangeCallback pageCb;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityStatusOrdersBinding.inflate(inflater, container, false); // ✅ ĐÚNG
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        if (getArguments() != null) {
            initialSelectedTab = getArguments().getInt("selectedTab", 0);
            Log.d(TAG_ACTIVITY, "Received initial selectedTab: " + initialSelectedTab);
        }
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        final ViewPager2 pager = binding.viewPager; // id phải tồn tại trong activity_status_orders.xml
        pagerRef = pager;
        pager.setAdapter(new ConfirmViewPagerAdapter(this));
        
        if (getActivity() instanceof MainActivity) {
            TabLayout mainTabLayout = getActivity().findViewById(R.id.order_tabs_layout); // id của TabLayout trong Activity
            tabLayoutRef = mainTabLayout;
            
            if (mainTabLayout != null) {
                mediator = new TabLayoutMediator(mainTabLayout, pager, (tab, pos) -> tab.setText(tabTitles[pos]));
                mediator.attach();
                
                pageCb = new ViewPager2.OnPageChangeCallback() {
                    @Override public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        sharedViewModel.updateTitle("Đơn hàng " + tabTitles[position]);
                    }
                };
                pager.registerOnPageChangeCallback(pageCb);
                
                pager.setCurrentItem(initialSelectedTab, false);
                sharedViewModel.updateTitle("Đơn hàng " + tabTitles[initialSelectedTab]);
            } else {
                Log.e(TAG_ACTIVITY, "Could not find order_tabs_layout!");
            }
        } else {
            Log.e(TAG_ACTIVITY, "Activity is not MainActivity!");
        }
    }
    
    private static class ConfirmViewPagerAdapter extends FragmentStateAdapter {
        ConfirmViewPagerAdapter(@NonNull Fragment fragment) { super(fragment); }
        @NonNull @Override public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new ConfirmationFragment();
                case 1: return new DeliveringFragment();
                case 2: return new BoughtFragment();
                case 3: return new CancelFragment();
                case 4: return new RefundFragment();
                default: return new ConfirmationFragment();
            }
        }
        @Override public int getItemCount() { return 5; }
    }
    
    @Override
    public void onDestroyView() {
        try {
            if (pagerRef != null && pageCb != null) pagerRef.unregisterOnPageChangeCallback(pageCb);
            if (mediator != null) mediator.detach();
        } catch (Exception e) {
            Log.w(TAG_ACTIVITY, "Cleanup error in onDestroyView", e);
        }
        pageCb = null;
        mediator = null;
        tabLayoutRef = null;
        pagerRef = null;
        binding = null;
        super.onDestroyView();
    }
}
