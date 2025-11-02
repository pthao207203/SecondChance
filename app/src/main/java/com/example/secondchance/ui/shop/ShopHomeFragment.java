package com.example.secondchance.ui.shop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.databinding.FragmentShopHomeBinding;
import com.google.android.material.card.MaterialCardView;

public class ShopHomeFragment extends Fragment {

    private FragmentShopHomeBinding binding;

    // === TAB (chỉ dùng 3 cái) ===
    private MaterialCardView tabFixed, tabNegotiation, tabAuction;

    // === NỘI DUNG ===
    private LinearLayout contentFixed, contentNegotiation, contentAuction;

    // === THANH CHỈ THỊ ===
    private View indicatorFixed, indicatorNegotiation, indicatorAuction;

    // === MÀU ===
    private static final int COLOR_SELECTED = R.color.highlight4blur;
    private static final int COLOR_UNSELECTED = R.color.grayDay;
    private static final int INDICATOR_SELECTED = R.color.highLight5;
    private static final int INDICATOR_UNSELECTED = R.color.whiteDay;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentShopHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // === NHẬN DỮ LIỆU SHOP (nếu có) ===
        if (getArguments() != null) {
            String shopName = getArguments().getString("shopName", "Cá Biết Bay");
            String shopPhone = getArguments().getString("shopPhone", "0333 333 333");
            String shopAddress = getArguments().getString("shopAddress", "Thôn Cá, xã Biết, quận Bay");
            String shopEmail = getArguments().getString("shopEmail", "Cabietbay@gmail.com");
            int shopAvatar = getArguments().getInt("shopAvatar", R.drawable.avatar1);

            binding.tvName.setText(shopName);
            binding.tvPhone.setText(shopPhone);
            binding.tvAddress.setText(shopAddress);
            binding.tvEmail.setText(shopEmail);

            Glide.with(this)
                    .load(shopAvatar)
                    .placeholder(R.drawable.avatar1)
                    .into(binding.ivAvatar);
        }

        // === NÚT XEM ĐÁNH GIÁ ===
        binding.btnViewReview.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_shop_home_to_comment));

        // === ÁNH XẠ TAB ===
        tabFixed = view.findViewById(R.id.tabFixed);
        tabNegotiation = view.findViewById(R.id.tabNegotiation);
        tabAuction = view.findViewById(R.id.tabAuction);

        // === ÁNH XẠ NỘI DUNG ===
        contentFixed = view.findViewById(R.id.contentFixed);
        contentNegotiation = view.findViewById(R.id.contentNegotiation);
        contentAuction = view.findViewById(R.id.contentAuction);

        // === ÁNH XẠ THANH CHỈ THỊ ===
        indicatorFixed = view.findViewById(R.id.indicatorFixed);
        indicatorNegotiation = view.findViewById(R.id.indicatorNegotiation);
        indicatorAuction = view.findViewById(R.id.indicatorAuction);

        // === MẶC ĐỊNH: TAB CỐ ĐỊNH ===
        selectTab(tabFixed, contentFixed, indicatorFixed);

        // === CLICK LISTENER ===
        tabFixed.setOnClickListener(v -> selectTab(tabFixed, contentFixed, indicatorFixed));
        tabNegotiation.setOnClickListener(v -> selectTab(tabNegotiation, contentNegotiation, indicatorNegotiation));
        tabAuction.setOnClickListener(v -> selectTab(tabAuction, contentAuction, indicatorAuction));
    }

    // === HÀM CHỌN TAB ===
    private void selectTab(MaterialCardView selectedTab, LinearLayout contentToShow, View selectedIndicator) {
        resetAllTabs();

        selectedTab.setCardBackgroundColor(getResources().getColor(COLOR_SELECTED));
        selectedIndicator.setBackgroundColor(getResources().getColor(INDICATOR_SELECTED));
        contentToShow.setVisibility(View.VISIBLE);
    }

    // === RESET TẤT CẢ TAB ===
    private void resetAllTabs() {
        // Reset màu nền
        if (tabFixed != null) tabFixed.setCardBackgroundColor(getResources().getColor(COLOR_UNSELECTED));
        if (tabNegotiation != null) tabNegotiation.setCardBackgroundColor(getResources().getColor(COLOR_UNSELECTED));
        if (tabAuction != null) tabAuction.setCardBackgroundColor(getResources().getColor(COLOR_UNSELECTED));

        // Reset thanh chỉ thị
        if (indicatorFixed != null) indicatorFixed.setBackgroundColor(getResources().getColor(INDICATOR_UNSELECTED));
        if (indicatorNegotiation != null) indicatorNegotiation.setBackgroundColor(getResources().getColor(INDICATOR_UNSELECTED));
        if (indicatorAuction != null) indicatorAuction.setBackgroundColor(getResources().getColor(INDICATOR_UNSELECTED));

        // Ẩn nội dung
        if (contentFixed != null) contentFixed.setVisibility(View.GONE);
        if (contentNegotiation != null) contentNegotiation.setVisibility(View.GONE);
        if (contentAuction != null) contentAuction.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
