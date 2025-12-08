package com.example.secondchance.ui.negotiation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.secondchance.databinding.FragmentNegotiationBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class NegotiationFragment extends Fragment {
    
    private FragmentNegotiationBinding binding;
    private SharedViewModel sharedViewModel;
    
    private final String[] tabTitles = new String[]{
      "Đã gửi", "Chấp nhận", "Đã hủy", "Đã mua"
    };
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNegotiationBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        NegotiationViewPagerAdapter viewPagerAdapter = new NegotiationViewPagerAdapter(this);
        binding.viewPager.setAdapter(viewPagerAdapter);
        
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
          (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
        
        // mặc định: Đã gửi
        updateNegotiationTitle(0);
        
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateNegotiationTitle(tab.getPosition());
            }
            
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    // ================== HÀM HELPER UPDATE TITLE ==================
    private void updateNegotiationTitle(int position) {
        if (position >= 0 && position < tabTitles.length) {
            String newTitle = "Thương lượng - " + tabTitles[position];
            sharedViewModel.updateTitle(newTitle);
        } else {
            sharedViewModel.updateTitle("Thương lượng");
        }
    }
    
    // ================== HÀM PUBLIC ĐỂ NHẢY TAB NHỎ ==================
    public void selectTab(int index) {
        if (binding == null) return;
        if (index < 0 || index >= tabTitles.length) return;
        
        binding.viewPager.setCurrentItem(index, true);
        updateNegotiationTitle(index);
    }
    
    /** Nhảy sang tab "Đã gửi" (index = 0) */
    public void openSentTab() {
        selectTab(0);
    }
    
    /** Nếu sau này cần, có thể thêm luôn: */
    public void openAcceptedTab() { selectTab(1); }
    public void openCancelledTab() { selectTab(2); }
    public void openCompletedTab() { selectTab(3); }
    
    // ================== ADAPTER 4 TAB CON ==================
    private class NegotiationViewPagerAdapter extends FragmentStateAdapter {
        
        public NegotiationViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }
        
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new NegotiationRequestFragment();
                case 1: return new NegotiationAcceptedFragment();
                case 2: return new NegotiationCancelledFragment();
                case 3: return new NegotiationCompletedFragment();
                default: return new NegotiationRequestFragment();
            }
        }
        
        @Override
        public int getItemCount() {
            return tabTitles.length;
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
