package com.example.secondchance.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;
import com.example.secondchance.R;
import com.example.secondchance.ui.product.adapter.ProductViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProductTabFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button btnAddProduct; // Nút "Thêm sản phẩm mới"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_tab, container, false);

        initViews(view);
        setupViewPager();
        setupButton();

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        btnAddProduct = view.findViewById(R.id.btn_add_product); // ID từ layout của bạn
    }

    private void setupViewPager() {
        ProductViewPagerAdapter adapter = new ProductViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Cố định");
                    break;
                case 1:
                    tab.setText("Thương lượng");
                    break;
                case 2:
                    tab.setText("Đấu giá");
                    break;
                case 3:
                    tab.setText("Đã xóa");
                    break;
            }
        }).attach();

        // Listen to tab changes để update button text
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateButtonBasedOnTab(position);
            }
        });
    }

    private void setupButton() {
        if (btnAddProduct != null) {
            btnAddProduct.setOnClickListener(v -> {
                int currentTab = viewPager.getCurrentItem();
                navigateToAddProduct(currentTab);
            });
        }
    }

    private void updateButtonBasedOnTab(int position) {
        if (btnAddProduct == null) return;

        switch (position) {
            case 0: // Cố định
            case 1: // Thương lượng
            case 2: // Đấu giá
                btnAddProduct.setText("Thêm sản phẩm mới");
                break;
            case 3:
                btnAddProduct.setVisibility(View.GONE); // Ẩn button ở tab "Đã xóa"
                return;
        }
        btnAddProduct.setVisibility(View.VISIBLE);
    }

    private void navigateToAddProduct(int tabPosition) {
        String productType;
        int navigationAction;

        switch (tabPosition) {
            case 0:
                productType = "fixed";
                navigationAction = R.id.action_productTab_to_addFixed; // ĐÃ ĐÚNG
                break;
            case 1:
                productType = "negotiable";
                navigationAction = R.id.action_productTab_to_addNegotiable;
                break;
            case 2:
                productType = "auction";
                navigationAction = R.id.action_productTab_to_addAuction;
                break;
            default:
                return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("productType", productType);

        Navigation.findNavController(requireView())
                .navigate(navigationAction, bundle);
    }
}
