package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import android.widget.LinearLayout;


import com.bumptech.glide.Glide;
import com.example.secondchance.R;

public class ProfileFragment extends Fragment {

    private TextView tvPendingCount;
    private TextView tvShippingCount;
    private TextView tvName, tvPhone, tvAddress, tvEmail;
    private ImageView ivAvatar;
    private ProfileViewModel viewModel;
    private AppCompatButton btnTest;
    private AppCompatButton btnBecomeSeller;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
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

        // Khởi tạo views
        initViews(view);

        // Setup badge counts
        setupBadgeCounts();

        // Setup click listeners
        setupClickListeners(view);

        // Observe LiveData from ViewModel
        observeViewModel();
    }

    private void initViews(View view) {
        tvPendingCount = view.findViewById(R.id.tvPendingCount);
        tvShippingCount = view.findViewById(R.id.tvShippingCount);
        tvName = view.findViewById(R.id.tvName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnTest = view.findViewById(R.id.btn_test);

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
        // TODO: Lấy dữ liệu thực từ API hoặc database
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
        // Lấy NavController
        final NavController navController = Navigation.findNavController(view);

        btnTest.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_profile_to_productTab));

        View.OnClickListener editProfileClickListener = v -> Navigation.findNavController(v).navigate(R.id.action_profile_to_editProfile);

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

        view.findViewById(R.id.tabLogout).setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Đăng xuất", Toast.LENGTH_SHORT).show();
            // TODO: Implement logout logic
        });

        // Wallet Detail
        view.findViewById(R.id.tvViewDetails).setOnClickListener(v ->
                Toast.makeText(requireContext(), "Xem chi tiết ví", Toast.LENGTH_SHORT).show()
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

        // Become Seller
        view.findViewById(R.id.btnBecomeSeller).setOnClickListener(v -> {
            navController.navigate(R.id.action_profile_to_rule_seller);
        });
    }
    private void setupListeners() {
        // Test button - Navigate to Product Tab
        if (btnTest != null) {
            btnTest.setOnClickListener(v -> {
                Navigation.findNavController(v)
                        .navigate(R.id.action_profile_to_productTab);
            });
        }

        // Become Seller button
        if (btnBecomeSeller != null) {
            btnBecomeSeller.setOnClickListener(v -> {
                // TODO: Implement become seller logic
                Navigation.findNavController(v)
                        .navigate(R.id.action_profile_to_productTab);
            });
        }
    }

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
