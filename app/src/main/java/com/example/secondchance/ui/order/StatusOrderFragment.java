package com.example.secondchance.ui.order;

import com.example.secondchance.ui.order.DeliveringFragment.DeliveringOrderNavigationListener;
import com.example.secondchance.ui.order.BoughtFragment.BoughtOrderNavigationListener;
//import com.example.secondchance.ui.order.RefundFragment.RefundOrderNavigationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.lifecycle.ViewModelProvider;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ActivityStatusOrdersBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.data.model.Order;
import java.io.Serializable;

public class StatusOrderFragment extends Fragment implements BoughtOrderNavigationListener, DeliveringOrderNavigationListener {

    private static final String TAG_ACTIVITY = "DBG_ORDER_FRAGMENT";
    private static final String TAG_PAGER = "DBG_ORDER_PAGER";
    private ActivityStatusOrdersBinding binding;
    private SharedViewModel sharedViewModel;

    private int initialSelectedTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityStatusOrdersBinding.inflate(inflater, container, false);
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
        Log.d(TAG_ACTIVITY, "onViewCreated - start");

        ConfirmViewPagerAdapter viewPagerAdapter = new ConfirmViewPagerAdapter(this);
        binding.viewPager.setAdapter(viewPagerAdapter);


        TabLayout localTabLayout = binding.orderTabLayoutLocal;
        String[] titles = sharedViewModel.getTabTitles();

        new TabLayoutMediator(localTabLayout, binding.viewPager,
                (tab, position) -> tab.setText(titles[position])
        ).attach();

        updateInitialTitle(initialSelectedTab);
        binding.viewPager.setCurrentItem(initialSelectedTab, false);

        observeViewModel();

        localTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                String newTitle = "Đơn hàng " + titles[position];
                sharedViewModel.updateTitle(newTitle);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        Log.d(TAG_ACTIVITY, "onViewCreated - end");
    }

    private void observeViewModel() {

        sharedViewModel.getRequestedTab().observe(getViewLifecycleOwner(), tabIndex -> {
            if (tabIndex != null) {
                Log.d(TAG_ACTIVITY, "ViewModel requested tab change to: " + tabIndex);
                binding.viewPager.setCurrentItem(tabIndex, false);
                sharedViewModel.clearTabRequest();
            }
        });

    }

    private void updateInitialTitle(int position) {
        String[] tabTitles = sharedViewModel.getTabTitles();
        if (position >= 0 && position < tabTitles.length) {
            String initialTitle = "Đơn hàng " + tabTitles[position];
            sharedViewModel.updateTitle(initialTitle);
        } else {
            sharedViewModel.updateTitle("Đơn hàng");
        }
        Log.d(TAG_ACTIVITY, "Initial title updated");
    }

    @Override
    public void navigateToBoughtDetail(String orderId, boolean isEvaluated) {
        // Gọi hàm navigateToDetail chung, sử dụng action và truyền trạng thái đánh giá (isEvaluated)
        navigateToDetail(orderId, R.id.action_orderFragment_to_boughtOrderDetailFragment, isEvaluated);
    }

    @Override
    public void navigateToDeliveringDetail(String orderId, Order.DeliveryOverallStatus status) {
        navigateToDetail(orderId, R.id.action_orderFragment_to_deliveringOrderDetailFragment, status);
    }

    public void navigateToDetail(String orderId, int actionId, @Nullable Serializable detailType) {
        Log.d(TAG_ACTIVITY, "navigateToDetail called - ID: " + orderId + ", Action: " + actionId + ", Type: " + detailType);

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        Bundle args = new Bundle();
        args.putString("orderId", orderId);

        if (actionId == R.id.action_orderFragment_to_confirmOrderDetailFragment) {
            if (detailType != null && detailType instanceof Order.OrderType) {
                args.putSerializable("orderType", detailType);
            } else {
                Log.e(TAG_ACTIVITY, "Error: orderType is required but was null/wrong type.");
                Toast.makeText(getContext(), "Thiếu thông tin loại đơn hàng!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (actionId == R.id.action_orderFragment_to_deliveringOrderDetailFragment) {
            if (detailType != null && detailType instanceof Order.DeliveryOverallStatus) {
                args.putSerializable("deliveryStatus", detailType);
            } else {
                Log.w(TAG_ACTIVITY, "DeliveryOverallStatus not sent/required.");
            }
        }

        if (actionId == R.id.action_orderFragment_to_boughtOrderDetailFragment) {
            if (detailType != null && detailType instanceof Boolean) {
                args.putBoolean("isEvaluated", (Boolean) detailType);
            } else {
                Log.e(TAG_ACTIVITY, "Error: isEvaluated (Boolean) is required...");
                Toast.makeText(getContext(), "Thiếu thông tin đánh giá!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (actionId == R.id.action_orderFragment_to_canceledOrderDetailFragment) {
            Log.d(TAG_ACTIVITY, "-> Navigating to CanceledDetail");
        }

        if (actionId == R.id.action_orderFragment_to_refundOrderDetailFragment) {
            if (detailType != null && detailType instanceof Order.RefundStatus) {
                args.putSerializable("refundStatus", detailType);
            } else {
                Log.e(TAG_ACTIVITY, "Error: RefundStatus is required...");
                Toast.makeText(getContext(), "Thiếu thông tin trạng thái hoàn trả!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            String actionName = getResources().getResourceName(actionId);
            Log.d(TAG_ACTIVITY, "Navigating with action: " + actionName);
            navController.navigate(actionId, args);
        } catch (Exception e) {
            Log.e(TAG_ACTIVITY, "Navigation failed! Action ID: " + actionId, e);
            Toast.makeText(getContext(), "Lỗi chuyển trang chi tiết.", Toast.LENGTH_SHORT).show();
        }
    }

    private class ConfirmViewPagerAdapter extends FragmentStateAdapter {
        public ConfirmViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new ConfirmationFragment();
                case 1: return new DeliveringFragment();
                case 2: return new BoughtFragment();
                case 3: return new CancelFragment();
                case 4: return new RefundFragment();
                default: return new ConfirmationFragment();
            }
        }

        @Override
        public int getItemCount() {
            return sharedViewModel.getTabTitles().length;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Log.d(TAG_ACTIVITY, "onDestroyView called");
    }
}