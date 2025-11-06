package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;

public class ProfileFragment extends Fragment {

    private TextView tvPendingCount, tvShippingCount;
    private TextView tvName, tvPhone, tvAddress, tvEmail;
    private ImageView ivAvatar;
    private ProfileViewModel viewModel;
    private SellerViewModel sellerViewModel;

    // Seller layouts
    private LinearLayout layoutChatReview;
    private LinearLayout layoutShopStatistics;
    private LinearLayout layoutProductsOrders;
    private LinearLayout layoutNegotiationsDashboards;
    private LinearLayout layoutNameShop;
    private TextView tvNoProduct;
    private AppCompatButton btnBecomeSeller;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        sellerViewModel = new ViewModelProvider(requireActivity()).get(SellerViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupBadgeCounts(); // Gọi để hiển thị badge
        setupClickListeners(view);
        observeViewModel();
        observeSellerStatus(); // Quan trọng: ẩn/hiện phần shop
    }

    private void initViews(View view) {
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvShippingCount = view.findViewById(R.id.tvShippingCount);
        tvName = view.findViewById(R.id.tvName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        tvNoProduct = view.findViewById(R.id.tvNoProduct);
        btnBecomeSeller = view.findViewById(R.id.btnBecomeSeller);

        layoutChatReview = view.findViewById(R.id.Chat_Review);
        layoutShopStatistics = view.findViewById(R.id.ShopStatistics);
        layoutProductsOrders = view.findViewById(R.id.Products_Orders);
        layoutNegotiationsDashboards = view.findViewById(R.id.Negoitations_Dashboards);
        layoutNameShop = view.findViewById(R.id.NameShop);
    }

    private void observeSellerStatus() {
        sellerViewModel.getIsSeller().observe(getViewLifecycleOwner(), isSeller -> {
            boolean isSellerMode = isSeller != null && isSeller;

            layoutChatReview.setVisibility(isSellerMode ? View.VISIBLE : View.GONE);
            layoutShopStatistics.setVisibility(isSellerMode ? View.VISIBLE : View.GONE);
            layoutProductsOrders.setVisibility(isSellerMode ? View.VISIBLE : View.GONE);
            layoutNegotiationsDashboards.setVisibility(isSellerMode ? View.VISIBLE : View.GONE);
            layoutNameShop.setVisibility(isSellerMode ? View.VISIBLE : View.GONE);
            tvNoProduct.setVisibility(isSellerMode ? View.GONE : View.VISIBLE);
            btnBecomeSeller.setVisibility(isSellerMode ? View.GONE : View.VISIBLE);
        });
    }

    private void observeViewModel() {
        viewModel.getName().observe(getViewLifecycleOwner(), name -> tvName.setText(name));
        viewModel.getPhone().observe(getViewLifecycleOwner(), phone -> tvPhone.setText(phone));
        viewModel.getEmail().observe(getViewLifecycleOwner(), email -> tvEmail.setText(email));

        viewModel.getAvatarUri().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) {
                Glide.with(this).load(uri).into(ivAvatar);
            }
        });

        viewModel.getAddressList().observe(getViewLifecycleOwner(), addressList -> {
            AddressItem defaultAddress = viewModel.getDefaultAddress();
            tvAddress.setText(defaultAddress != null ? defaultAddress.getAddress() : "Chưa có địa chỉ mặc định");
        });
    }

    private void setupBadgeCounts() {
        int pendingCount = 0;
        int shippingCount = 17;

        tvPendingCount.setText(String.valueOf(pendingCount));
        tvPendingCount.setVisibility(pendingCount > 0 ? View.VISIBLE : View.GONE);

        tvShippingCount.setText(String.valueOf(shippingCount));
        tvShippingCount.setVisibility(shippingCount > 0 ? View.VISIBLE : View.GONE);
    }

    private void setupClickListeners(View view) {
        NavController navController = Navigation.findNavController(view);

        // Avatar + Name → Edit Profile
        View.OnClickListener editProfile = v -> navController.navigate(R.id.action_profile_to_editProfile);
        ivAvatar.setOnClickListener(editProfile);
        tvName.setOnClickListener(editProfile);

        // Top Tabs
        view.findViewById(R.id.tabAccountInfo).setOnClickListener(v -> Toast.makeText(requireContext(), "Điều khoản", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.tabSupport).setOnClickListener(v -> Toast.makeText(requireContext(), "Hỗ trợ", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.tabSettings).setOnClickListener(v -> navController.navigate(R.id.action_profile_to_settings));
        view.findViewById(R.id.tabLogout).setOnClickListener(v -> Toast.makeText(requireContext(), "Đăng xuất", Toast.LENGTH_SHORT).show());

        // Wallet
        view.findViewById(R.id.tvViewDetails).setOnClickListener(v -> navController.navigate(R.id.action_profile_to_wallet));

        // Order History Tabs
        int[] tabs = {0, 1, 2, 3, 4};
        int[] ids = {R.id.btnPending, R.id.btnShipping, R.id.btnPurchased, R.id.btnCancelled, R.id.btnRefund};
        for (int i = 0; i < ids.length; i++) {
            View btn = view.findViewById(ids[i]);
            if (btn != null) {
                int tab = tabs[i];
                btn.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putInt("selectedTab", tab);
                    navController.navigate(R.id.action_profile_to_orderFragment, args);
                });
            }
        }

        // Become Seller Button
        btnBecomeSeller.setOnClickListener(v -> navController.navigate(R.id.action_profile_to_rule_seller));

        // Test button (nếu có)
        View btnTest = view.findViewById(R.id.btn_test);
        if (btnTest != null) {
            btnTest.setOnClickListener(v -> navController.navigate(R.id.action_profile_to_productTab));
        }
    }

    // Hàm public để update badge từ bên ngoài (nếu cần)
    public void updateBadgeCounts(int pendingCount, int shippingCount) {
        if (tvPendingCount != null) {
            tvPendingCount.setText(String.valueOf(pendingCount));
            tvPendingCount.setVisibility(pendingCount > 0 ? View.VISIBLE : View.GONE);
        }
        if (tvShippingCount != null) {
            tvShippingCount.setText(String.valueOf(shippingCount));
            tvShippingCount.setVisibility(shippingCount > 0 ? View.VISIBLE : View.GONE);
        }
    }
}

