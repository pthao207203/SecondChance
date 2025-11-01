package com.example.secondchance.ui.product.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.secondchance.ui.product.list.ProductListAuctionFragment;
import com.example.secondchance.ui.product.list.ProductListDeletedFragment;
import com.example.secondchance.ui.product.list.ProductListFragment;
import com.example.secondchance.ui.product.list.ProductListNegotiableFragment;

public class ProductViewPagerAdapter extends FragmentStateAdapter {

    public ProductViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ProductListFragment.newInstance("fixed");
            case 1:
                return ProductListNegotiableFragment.newInstance("negotiable");
            case 2:
                return ProductListAuctionFragment.newInstance("auction");
            case 3:
                return ProductListDeletedFragment.newInstance("deleted");
            default:
                return ProductListFragment.newInstance("fixed");
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Fixed, Negotiable, Auction, Deleted
    }
}
