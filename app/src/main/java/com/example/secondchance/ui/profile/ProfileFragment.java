package com.example.secondchance.ui.profile;

import android.content.Intent;
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
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.widget.LinearLayout;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.ui.auth.AuthActivity;
import com.example.secondchance.ui.auth.AuthManager;
import com.example.secondchance.ui.auth.LoginFragment;
import com.example.secondchance.util.Prefs;

public class ProfileFragment extends Fragment {

    private TextView tvPendingCount;
    private TextView tvShippingCount;
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
        setupBadgeCounts();
        setupClickListeners(view);
        observeViewModel();
        observeSellerStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        ensureLoggedInOrRedirect();
    }

    private void ensureLoggedInOrRedirect() {
        String token = Prefs.getToken(requireContext());
        if (token == null || token.trim().isEmpty()) {
            NavController nav = NavHostFragment.findNavController(this);

            // Pop về gốc graph để không back về Profile
            NavOptions opts = new NavOptions.Builder()
              .setPopUpTo(nav.getGraph().getStartDestinationId(), true)
              .setLaunchSingleTop(true)
              .build();

            // ⚠️ Điều hướng thẳng tới đích (không cần action)
            goToAuthAndFinish();
        }
    }
    private void goToAuthAndFinish() {
        if (!isAdded()) return;
        NavController nav = NavHostFragment.findNavController(this);
        nav.setGraph(R.navigation.mobile_navigation, null);
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
            if (defaultAddress != null) {
                tvAddress.setText(defaultAddress.getAddress());
            } else {
                tvAddress.setText("Chưa có địa chỉ mặc định");
            }
        });
    }


    private void setupBadgeCounts() {
        int pendingCount = 0;
        int shippingCount = 17;

        if (pendingCount > 0) {
            tvPendingCount.setText(String.valueOf(pendingCount));
            tvPendingCount.setVisibility(View.VISIBLE);
        } else {
            tvPendingCount.setVisibility(View.GONE);
        }

        if (shippingCount > 0) {
            tvShippingCount.setText(String.valueOf(shippingCount));
            tvShippingCount.setVisibility(View.VISIBLE);
        } else {
            tvShippingCount.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners(View view) {
        NavController navController = Navigation.findNavController(view);


        View.OnClickListener editProfileClickListener = v -> Navigation.findNavController(v).navigate(R.id.action_profile_to_editProfile);
        view.findViewById(R.id.ivAvatar).setOnClickListener(editProfileClickListener);
        view.findViewById(R.id.tvName).setOnClickListener(editProfileClickListener);

        // Top Tabs
        view.findViewById(R.id.tabAccountInfo).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Điều khoản", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.tabSupport).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Hỗ trợ", Toast.LENGTH_SHORT).show()
        );

        view.findViewById(R.id.tabSettings).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profile_to_settings);
        });

        // Wallet
        view.findViewById(R.id.tvViewDetails).setOnClickListener(v -> navController.navigate(R.id.action_profile_to_wallet));


        view.findViewById(R.id.tabLogout).setOnClickListener(v -> {
            AuthManager.getInstance(requireContext()).clear();
            Prefs.saveToken(requireContext(), ""); // nếu bạn dùng Prefs riêng cho token

            NavController nav = NavHostFragment.findNavController(ProfileFragment.this);
            nav.setGraph(R.navigation.nav_auth, null);

            nav.navigate(R.id.loginFragment);
        });

        view.findViewById(R.id.tvViewDetails).setOnClickListener(
          v -> Navigation.findNavController(v).navigate(R.id.action_profile_to_wallet)
        );

        // Order History
        LinearLayout btnPending = view.findViewById(R.id.btnPending);
        if (btnPending != null) {
            btnPending.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("selectedTab", 0);
                navController.navigate(R.id.action_profile_to_orderFragment, args);
            });
        }

        LinearLayout btnShipping = view.findViewById(R.id.btnShipping);
        if (btnShipping != null) {
            btnShipping.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("selectedTab", 1);
                navController.navigate(R.id.action_profile_to_orderFragment, args);
            });
        }

        LinearLayout btnPurchased = view.findViewById(R.id.btnPurchased);
        if (btnPurchased != null) {
            btnPurchased.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("selectedTab", 2);
                navController.navigate(R.id.action_profile_to_orderFragment, args);
            });
        }
        LinearLayout btnCancelled = view.findViewById(R.id.btnCancelled);
        if (btnCancelled != null) {
            btnCancelled.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("selectedTab", 3);
                navController.navigate(R.id.action_profile_to_orderFragment, args);
            });
        }

        LinearLayout btnRefund = view.findViewById(R.id.btnRefund);
        if (btnRefund != null) {
            btnRefund.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putInt("selectedTab", 4);
                navController.navigate(R.id.action_profile_to_orderFragment, args);
            });
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
            if (pendingCount > 0) {
                tvPendingCount.setText(String.valueOf(pendingCount));
                tvPendingCount.setVisibility(View.VISIBLE);
            } else {
                tvPendingCount.setVisibility(View.GONE);
            }
        }

        if (tvShippingCount != null) {
            if (shippingCount > 0) {
                tvShippingCount.setText(String.valueOf(shippingCount));
                tvShippingCount.setVisibility(View.VISIBLE);
            } else {
                tvShippingCount.setVisibility(View.GONE);
            }
        }
    }
}

