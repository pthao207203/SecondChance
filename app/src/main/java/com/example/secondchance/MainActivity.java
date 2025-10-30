package com.example.secondchance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.secondchance.databinding.ActivityMainBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator; // THÊM IMPORT NÀY

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;
  private NavController navController;
  private SharedViewModel sharedViewModel;
  private boolean backBusy = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      Log.e("FATAL", "Uncaught crash on thread " + t.getName(), e);
    });
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

    // LẮNG NGHE VIEWPAGER TỪ STATUSORDERFRAGMENT ĐỂ KẾT NỐI TABLAYOUT
    sharedViewModel.getViewPager().observe(this, viewPager -> {

      // Lấy TabLayout từ binding (đảm bảo ID là 'order_tabs_layout')
      TabLayout mainTabLayout = binding.orderTabsLayout;

      if (viewPager != null) {
        // ViewPager đã sẵn sàng, kết nối nó với TabLayout
        Log.d("MainActivity", "ViewPager received, attaching TabLayoutMediator.");
        String[] titles = sharedViewModel.getTabTitles();

        // Gắn Mediator
        new TabLayoutMediator(mainTabLayout, viewPager, (tab, position) -> {
          tab.setText(titles[position]);
        }).attach();

        // Thêm listener (logic này được chuyển từ StatusOrderFragment sang)
        mainTabLayout.clearOnTabSelectedListeners();
        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
            int position = tab.getPosition();
            String newTitle = "Đơn hàng " + titles[position];
            sharedViewModel.updateTitle(newTitle); // Cập nhật title
            if (viewPager.getCurrentItem() != position) {
              viewPager.setCurrentItem(position, true); // Đồng bộ ViewPager
            }
          }
          @Override public void onTabUnselected(TabLayout.Tab tab) {}
          @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Đồng bộ Tab khi ViewPager được vuốt
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            if (mainTabLayout.getSelectedTabPosition() != position) {
              mainTabLayout.selectTab(mainTabLayout.getTabAt(position));
            }
          }
        });

        // Set tab ban đầu
        int currentItem = viewPager.getCurrentItem();
        if (mainTabLayout.getSelectedTabPosition() != currentItem) {
          // Dùng post để đảm bảo TabLayout đã sẵn sàng
          mainTabLayout.post(() -> mainTabLayout.selectTab(mainTabLayout.getTabAt(currentItem)));
        }

      } else {
        // Khi StatusOrderFragment bị hủy (viewPager == null)
        Log.d("MainActivity", "ViewPager cleared, detaching TabLayout.");
        mainTabLayout.clearOnTabSelectedListeners();
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

      // Back mặc định
      wireBackIcon(iconBack);

      // Tiêu đề
      if (destinationId == R.id.navigation_order) {
        String t = sharedViewModel.getCurrentTitle().getValue();
        tvTitle.setText(t != null ? t : "Đơn hàng");
        Log.d("MainActivity", "Header Title (Order): " + tvTitle.getText());
      } else {
        CharSequence label = destination.getLabel();
        tvTitle.setText(label != null ? label : "");
        Log.d("MainActivity", "Header Title (Other): " + label);
      }
    }
  }

  private void wireBackIcon(View iconBack) {
    iconBack.setOnClickListener(v -> {
      if (backBusy) return;
      backBusy = true;
      v.postDelayed(() -> backBusy = false, 400);
      try {
        if (!navController.popBackStack()) navController.navigateUp();
      } catch (Exception e) {
        Log.e("MainActivity", "Back navigate error", e);
      }
    });
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

  // --- Các hàm xử lý chung khi click icon ---
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