package com.example.secondchance.ui.order;

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
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.example.secondchance.MainActivity;
import com.example.secondchance.R;
import com.example.secondchance.databinding.ActivityStatusOrdersBinding;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.lifecycle.ViewModelProvider;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.data.model.Order;

public class OrderFragment extends Fragment {

    private static final String TAG_ACTIVITY = "DBG_ORDER_FRAGMENT";
    private static final String TAG_PAGER = "DBG_ORDER_PAGER";
    private ActivityStatusOrdersBinding binding;
    private SharedViewModel sharedViewModel;
    private final String[] tabTitles = new String[]{"Xác nhận", "Đang giao", "Đã mua", "Đã hủy", "Hoàn trả"};
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

        // Kết nối Tab từ Activity
        if (getActivity() instanceof MainActivity) {
            TabLayout mainTabLayout = getActivity().findViewById(R.id.order_tabs_layout);
            if (mainTabLayout != null) {
                new TabLayoutMediator(mainTabLayout, binding.viewPager, (tab, position) -> tab.setText(tabTitles[position])).attach();
                Log.d(TAG_ACTIVITY, "TabLayoutMediator attached");

                mainTabLayout.clearOnTabSelectedListeners();
                mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        int position = tab.getPosition();
                        String newTitle = "Đơn hàng " + tabTitles[position];
                        sharedViewModel.updateTitle(newTitle);
                        binding.viewPager.setCurrentItem(tab.getPosition(), true);
                    }
                    @Override public void onTabUnselected(TabLayout.Tab tab) {}
                    @Override public void onTabReselected(TabLayout.Tab tab) {}
                });

                int initialPosition = mainTabLayout.getSelectedTabPosition();
                updateInitialTitle(initialSelectedTab);
                binding.viewPager.setCurrentItem(initialSelectedTab, false);
                if (mainTabLayout.getSelectedTabPosition() != initialSelectedTab) {
                    // Dùng post để đảm bảo TabLayout đã sẵn sàng
                    mainTabLayout.post(() -> mainTabLayout.selectTab(mainTabLayout.getTabAt(initialSelectedTab)));
                }

                // Lắng nghe ViewPager cập nhật Tab
                binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        if (mainTabLayout.getSelectedTabPosition() != position) {
                            mainTabLayout.selectTab(mainTabLayout.getTabAt(position));
                        }
                    }
                });

            } else { Log.e(TAG_ACTIVITY, "Could not find order_tabs_layout!"); }
        } else { Log.e(TAG_ACTIVITY, "Activity is not MainActivity!"); }

        Log.d(TAG_ACTIVITY, "onViewCreated - end");
    }

    // cập nhật tiêu đề ban đầu
    private void updateInitialTitle(int position) {
        if (position >= 0 && position < tabTitles.length) {
            String initialTitle = "Đơn hàng " + tabTitles[position];
            sharedViewModel.updateTitle(initialTitle);
        } else {
            sharedViewModel.updateTitle("Đơn hàng");
        }
        Log.d(TAG_ACTIVITY, "Initial title updated");
    }


    public void navigateToDetail(String orderId, int actionId, @Nullable java.io.Serializable detailType) {
        Log.d(TAG_ACTIVITY, "navigateToDetail called - ID: " + orderId + ", Action: " + actionId + ", Type: " + detailType);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        Bundle args = new Bundle();
        args.putString("orderId", orderId);

        // chỉ gửi orderType nếu là ConfirmDetail
        if (actionId == R.id.action_orderFragment_to_confirmOrderDetailFragment) {
            if (detailType != null && detailType instanceof Order.OrderType) {
                args.putSerializable("orderType", detailType); // Gửi OrderType
                Log.d(TAG_ACTIVITY, "-> Sending OrderType for ConfirmDetail");
            } else {
                Log.e(TAG_ACTIVITY, "Error: orderType is required but was null/wrong type.");
                Toast.makeText(getContext(), "Thiếu thông tin loại đơn hàng!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // action là Delivering, gửi tham số deliveryStatus
        if (actionId == R.id.action_orderFragment_to_deliveringOrderDetailFragment) {
            if (detailType != null && detailType instanceof Order.DeliveryOverallStatus) {
                args.putSerializable("deliveryStatus", detailType);
                Log.d(TAG_ACTIVITY, "-> Sending DeliveryOverallStatus for DeliveringDetail");
            } else {
                Log.w(TAG_ACTIVITY, "DeliveryOverallStatus not sent/required for this action, skipping.");
            }
        }

        // BOUGHTDETAIL
        if (actionId == R.id.action_orderFragment_to_boughtOrderDetailFragment) {
            if (detailType != null && detailType instanceof Boolean) {
                args.putBoolean("isEvaluated", (Boolean) detailType);
                Log.d(TAG_ACTIVITY, "-> Sending isEvaluated for BoughtDetail");
            } else {
                Log.e(TAG_ACTIVITY, "Error: isEvaluated (Boolean) is required...");
                Toast.makeText(getContext(), "Thiếu thông tin đánh giá!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Refund
        if (actionId == R.id.action_orderFragment_to_refundOrderDetailFragment) {
            if (detailType != null && detailType instanceof Order.RefundStatus) {
                args.putSerializable("refundStatus", detailType);
                Log.d(TAG_ACTIVITY, "-> Sending RefundStatus for RefundDetail");
            } else {
                Log.e(TAG_ACTIVITY, "Error: RefundStatus is required...");
                Toast.makeText(getContext(), "Thiếu thông tin trạng thái hoàn trả!", Toast.LENGTH_SHORT).show();
                return; // Ngăn không cho navigate
            }
        }

        // Thực hiện navigate
        try {
            String actionName = getResources().getResourceName(actionId);
            Log.d(TAG_ACTIVITY, "Navigating with action: " + actionName);
            navController.navigate(actionId, args);
        } catch (Exception e) {
            Log.e(TAG_ACTIVITY, "Navigation failed! Action ID: " + actionId, e);
            Toast.makeText(getContext(), "Lỗi chuyển trang chi tiết.", Toast.LENGTH_SHORT).show();
        }
    }


    public void showOrderDetail(String orderId, Order.OrderType orderType) {
        Log.d(TAG_ACTIVITY,"showOrderDetail (shortcut) called");
        navigateToDetail(orderId, R.id.action_orderFragment_to_confirmOrderDetailFragment, orderType);
    }

    private class ConfirmViewPagerAdapter extends FragmentStateAdapter {
        public ConfirmViewPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Log.d(TAG_PAGER, "createFragment called position=" + position);
            switch (position) {
                case 0: return new ConfirmationFragment();
                case 1: return new DeliveringFragment();
                case 2: return new BoughtFragment();
                case 3: return new CancelFragment();
                case 4: return new RefundFragment();
                default: return new ConfirmationFragment();
            }
        }
        @Override public int getItemCount() { return tabTitles.length; }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}