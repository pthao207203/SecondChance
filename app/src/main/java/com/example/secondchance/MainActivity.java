package com.example.secondchance;

import androidx.navigation.NavOptions;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import com.example.secondchance.databinding.ActivityMainBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;
  private NavController navController;
  private SharedViewModel sharedViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    // Khởi tạo SharedViewModel
    sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment_activity_main);
    navController = navHostFragment.getNavController();

    setupIconClickListeners();
    setupBottomMenuClickListeners();

    // Lắng nghe sự kiện chuyển Fragment để ĐỔI GIAO DIỆN Header
    navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
      Log.d("MainActivity", "Destination changed to: " + destination.getLabel() + " (ID: " + destination.getId() + ")");
      updateUiVisibility(destination);
    });

    // Lắng nghe ViewModel để CẬP NHẬT TIÊU ĐỀ
    sharedViewModel.getCurrentTitle().observe(this, newTitle -> {
      Log.d("MainActivity", "ViewModel title updated: " + newTitle);
      NavDestination currentDestination = navController.getCurrentDestination();
      if (currentDestination != null) {
        int currentDestId = currentDestination.getId();
        // Chỉ cập nhật title nếu đang ở OrderFragment (navigation_order)
        if (currentDestId == R.id.navigation_order) {
          if (newTitle != null && !newTitle.isEmpty()) {
            binding.headerMain.tvHeaderTitle.setText(newTitle);
            Log.d("MainActivity", "Header title set from ViewModel: " + newTitle);
          } else {
            binding.headerMain.tvHeaderTitle.setText("Đơn hàng"); // Fallback
            Log.d("MainActivity", "Header title set to fallback 'Đơn hàng'");
          }
        }
      }
    });
  }

  // cập nhật GIAO DIỆN header (ẩn/hiện)
  private void updateUiVisibility(NavDestination destination) {
    if (binding == null || destination == null) return;

    int destinationId = destination.getId();
    Log.d("MainActivity", "Updating UI visibility for ID: " + destinationId);

    View searchContainer = binding.headerMain.searchContainer;
    View iconBack = binding.headerMain.iconBack;
    TextView tvTitle = binding.headerMain.tvHeaderTitle;
    View orderTabsAppBar = binding.orderTabsAppbar;

    // ẨN/HIỆN THANH TAB Chỉ hiện khi ở màn hình OrderFragment (navigation_order)
    if (destinationId == R.id.navigation_order) {
      orderTabsAppBar.setVisibility(View.VISIBLE);
      Log.d("MainActivity", "TabLayout VISIBLE");
    } else { // Ẩn ở tất cả màn hình khác (Home, Profile, Chi tiết...)
      orderTabsAppBar.setVisibility(View.GONE);
      Log.d("MainActivity", "TabLayout GONE");
    }

    // ẨN/HIỆN HEADER CHÍNH
    if (destinationId == R.id.navigation_home) { // Trang chủ
      searchContainer.setVisibility(View.VISIBLE);
      iconBack.setVisibility(View.GONE);
      tvTitle.setVisibility(View.GONE);
      Log.d("MainActivity", "Header: Show Search");

    } else { // Tất cả các màn hình khác (Profile, Order, Detail, AI, ...)
      searchContainer.setVisibility(View.GONE);
      iconBack.setVisibility(View.VISIBLE);
      tvTitle.setVisibility(View.VISIBLE);
      Log.d("MainActivity", "Header: Show Back/Title");

      // Cập nhật Tiêu đề Header VÀ Back
      if (destinationId == R.id.navigation_order) {
        iconBack.setOnClickListener(v -> {
          try {
            navController.navigate(R.id.action_order_to_profileFragment);
            Log.d("MainActivity", "Back button clicked: Navigating via action_order_to_profileFragment");
          } catch (Exception e) {
            Log.w("MainActivity", "Back action failed, falling back to navigateUp()", e);
            navController.navigateUp(); // Fallback nếu action bị lỗi
          }
        });

        String currentOrderTitle = sharedViewModel.getCurrentTitle().getValue();
        tvTitle.setText(currentOrderTitle != null ? currentOrderTitle : "Đơn hàng");
        Log.d("MainActivity", "Header Title (Order): " + tvTitle.getText());

      } else {
        // Các màn hình khác (Profile, Chi tiết, AI...) dùng navigateUp()
        iconBack.setOnClickListener(v -> navController.navigateUp());

        CharSequence label = destination.getLabel();
        tvTitle.setText(label != null ? label : "");
        Log.d("MainActivity", "Header Title (Other): " + label);
      }
    }
  }
  // sự kiện click cho 3 icon trên header
  private void setupIconClickListeners() {
    binding.headerMain.iconCart.setOnClickListener(v -> openCartScreen());
    binding.headerMain.iconChat.setOnClickListener(v -> openChatScreen());
    binding.headerMain.iconNotify.setOnClickListener(v -> openNotificationScreen());
    binding.headerMain.iconSearch.setOnClickListener(v -> Toast.makeText(this, "Tìm kiếm...", Toast.LENGTH_SHORT).show());
  }

  // sự kiện click cho menu dưới
  private void setupBottomMenuClickListeners() {
    NavOptions navOptions = new NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
            .build();

    binding.myCustomMenu.navigationHome.setOnClickListener(v -> {
      if (navController.getCurrentDestination().getId() != R.id.navigation_home) {
        navController.navigate(R.id.navigation_home, null, navOptions);
      }
    });

    binding.myCustomMenu.navigationDashboard.setOnClickListener(v -> {
      if (navController.getCurrentDestination().getId() != R.id.navigation_dashboard) {
        navController.navigate(R.id.navigation_dashboard, null, navOptions);
      }
    });
    binding.myCustomMenu.navigationNotifications.setOnClickListener(v -> {
      if (navController.getCurrentDestination().getId() != R.id.navigation_notifications) {
        navController.navigate(R.id.navigation_notifications, null, navOptions);
      }
    });
    binding.myCustomMenu.navigationProfile.setOnClickListener(v -> {
      if (navController.getCurrentDestination().getId() != R.id.navigation_profile) {
        navController.navigate(R.id.navigation_profile, null, navOptions);
      }
    });
  }

  private void openCartScreen() {
    Toast.makeText(this, "Mở Giỏ hàng", Toast.LENGTH_SHORT).show();
    // TODO: mở màn hình Giỏ hàng
  }
  private void openChatScreen() {
    Toast.makeText(this, "Mở Chat", Toast.LENGTH_SHORT).show();
    // TODO: mở màn hình Chat
  }
  private void openNotificationScreen() {
    Toast.makeText(this, "Mở Thông báo", Toast.LENGTH_SHORT).show();
    // TODO: mở màn hình Thông báo
  }
}
