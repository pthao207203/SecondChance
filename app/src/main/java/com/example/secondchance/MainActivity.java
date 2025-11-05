package com.example.secondchance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.example.secondchance.databinding.ActivityMainBinding;
import com.example.secondchance.viewmodel.SharedViewModel;
import com.example.secondchance.R;

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
    Log.d("MainActivityDebug", "MainActivity onCreate called");

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

        // check navigation_negotiation
        if (currentDestId == R.id.navigation_order ||
                currentDestId == R.id.navigation_negotiation) {

          if (newTitle != null && !newTitle.isEmpty()) {
            binding.headerMain.tvHeaderTitle.setText(newTitle);
            Log.d("MainActivity", "Header title set from ViewModel: " + newTitle);
          } else {
            if(currentDestId == R.id.navigation_order) {
              binding.headerMain.tvHeaderTitle.setText("Đơn hàng");
            } else {
              binding.headerMain.tvHeaderTitle.setText("Thương lượng");
            }
          }
        }
      }
    });

  }

  // GIAO DIỆN header (ẩn/hiện)
  private void updateUiVisibility(NavDestination destination) {
    if (binding == null || destination == null) return;

    int destinationId = destination.getId();
    Log.d("MainActivity", "Updating UI visibility for ID: " + destinationId);

    View searchContainer = binding.headerMain.searchContainer;
    View iconBack = binding.headerMain.iconBack;
    TextView tvTitle = binding.headerMain.tvHeaderTitle;

    // ẨN/HIỆN HEADER CHÍNH
    if (destinationId == R.id.navigation_home) {
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

      // logic tiêu đề cho navigation_negotiation
      if (destinationId == R.id.navigation_order) {
        String t = sharedViewModel.getCurrentTitle().getValue();
        tvTitle.setText(t != null ? t : "Đơn hàng");
        Log.d("MainActivity", "Header Title (Order): " + tvTitle.getText());

      } else if (destinationId == R.id.navigation_negotiation) {
        String t = sharedViewModel.getCurrentTitle().getValue();
        tvTitle.setText(t != null ? t : "Thương lượng");
        Log.d("MainActivity", "Header Title (Negotiation): " + tvTitle.getText());

      } else { // Các màn hình khác
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
    binding.myCustomMenu.navigationNegotiation.setOnClickListener(v -> {
      if (navController.getCurrentDestination().getId() != R.id.navigation_negotiation) {
        navController.navigate(R.id.navigation_negotiation, null, navOptions);
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
