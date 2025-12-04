package com.example.secondchance.ui.auction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.secondchance.databinding.FragmentMyAuctionBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyAuctionFragment extends Fragment {

    private FragmentMyAuctionBinding binding;
    private SharedViewModel sharedViewModel;

    private final String[] tabTitles = new String[]{
            "Đang đấu giá", "Thành công", "Thất bại"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyAuctionBinding.inflate(inflater, container, false);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MyAuctionViewPagerAdapter viewPagerAdapter = new MyAuctionViewPagerAdapter(this);
        binding.viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
        
        // Initial title update if needed, though parent TradeFragment might handle it
    }

    private class MyAuctionViewPagerAdapter extends FragmentStateAdapter {

        public MyAuctionViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new MyAuctionBiddingFragment();
                case 1: return new MyAuctionSuccessFragment();
                case 2: return new MyAuctionFailedFragment();
                default: return new MyAuctionBiddingFragment();
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
