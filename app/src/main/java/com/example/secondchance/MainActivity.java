package com.example.secondchance;

import static com.example.secondchance.util.Prefs.getToken;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.ActivityMainBinding;
import com.example.secondchance.ui.auth.AuthManager;
import com.example.secondchance.viewmodel.SharedViewModel;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;
  private NavController navController;
  private SharedViewModel sharedViewModel;
  private boolean backBusy = false;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Thread.setDefaultUncaughtExceptionHandler((t, e) ->
      Log.e("FATAL", "Uncaught crash on thread " + t.getName(), e)
    );
    super.onCreate(savedInstanceState);
    
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("vi"));
    RetrofitProvider.init(this);
    
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    Log.d("MainActivityDebug", "MainActivity onCreate called");
    
    // NavHost + NavController
    NavHostFragment navHostFragment =
      (NavHostFragment) getSupportFragmentManager()
        .findFragmentById(R.id.nav_host_fragment_activity_main);
    navController = navHostFragment.getNavController();
    
    // 3.1 Chọn graph ban đầu theo trạng thái đăng nhập (giữ y như bạn muốn)
    boolean isLoggedIn = AuthManager.getInstance(this).isLoggedIn();
    if (isLoggedIn) {
      navController.setGraph(R.navigation.mobile_navigation);      // 🔧 CHANGED (switch graph)
    } else {
      navController.setGraph(R.navigation.nav_auth);               // 🔧 CHANGED (switch graph)
    }
    
    // Gắn click sample của bạn (Home icon ở custom menu)
    binding.myCustomMenu.navigationHome.setOnClickListener(v -> {
      NavController c = navController;
      c.navigate(R.id.navigation_home);
    });
    
    // ViewModel
    sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
    sharedViewModel.getCurrentTitle().observe(this, this::applySharedTitleIfNeeded);
    
    setupIconClickListeners();
    setupBottomMenuClickListeners();
    
    // Lắng nghe chuyển đích để cập nhật UI khung
    navController.addOnDestinationChangedListener((controller, destination, args) -> {
      Log.d("MainActivity", "Destination changed: " + destination.getId() + " label=" + destination.getLabel());
      updateUiVisibility(destination);
    });
    
    // Cập nhật UI ngay lần đầu sau khi setGraph
    NavDestination cur = navController.getCurrentDestination();
    if (cur != null) updateUiVisibility(cur);                     // 🔧 CHANGED
  }
  
  private void applySharedTitleIfNeeded(String newTitle) {
    NavDestination cur = navController.getCurrentDestination();
    if (cur == null) return;
    int id = cur.getId();
    if (id == R.id.navigation_order || id == R.id.navigation_negotiation) {
      if (newTitle != null && !newTitle.isEmpty()) {
        binding.headerMain.tvHeaderTitle.setText(newTitle);
      } else {
        binding.headerMain.tvHeaderTitle.setText(
          id == R.id.navigation_order ? "Đơn hàng" : "Thương lượng"
        );
      }
    }
  }
  
  // GIAO DIỆN header (ẩn/hiện)
  private void updateUiVisibility(NavDestination destination) {
    if (binding == null || destination == null) return;
    
    int destinationId = destination.getId();
    Log.d("MainActivity", "Updating UI for dest ID: " + destinationId);
    
    View header = binding.headerMain.getRoot();
    View bottom = binding.myCustomMenu.getRoot();
    View tabsAppBar = binding.orderTabsAppbar;
    View searchContainer = binding.headerMain.searchContainer;
    View iconBack = binding.headerMain.iconBack;
    TextView tvTitle = binding.headerMain.tvHeaderTitle;
    
    // 🔧 CHANGED: xác định đang ở graph nào bằng ID của graph hiện tại
    View authWave = binding.authWave;
    
    boolean inAuth = false;
    try {
      navController.getGraph();
      inAuth = navController.getGraph().getId() == R.id.nav_auth;
    } catch (Exception ignore) {}
    
    // Ẩn/hiện UI khung chính
    header.setVisibility(inAuth ? View.GONE : View.VISIBLE);
    bottom.setVisibility(inAuth ? View.GONE : View.VISIBLE);
    authWave.setVisibility(inAuth ? View.VISIBLE : View.GONE);
    
    // Tab chỉ hiện khi ở Order (và đang ở main)
    boolean inOrder = destinationId == R.id.navigation_order;
    tabsAppBar.setVisibility(inOrder && !inAuth ? View.VISIBLE : View.GONE);
    
    if (!inAuth) {
      // Header: Home thì show thanh search, còn lại back + title
      boolean isHome = destinationId == R.id.navigation_home;
      searchContainer.setVisibility(isHome ? View.VISIBLE : View.GONE);
      iconBack.setVisibility(isHome ? View.GONE : View.VISIBLE);
      tvTitle.setVisibility(isHome ? View.GONE : View.VISIBLE);
      
      wireBackIcon(iconBack);
      
      if (!isHome && !inOrder && destination.getLabel() != null) {
        tvTitle.setText(destination.getLabel());
      } else if (inOrder) {
        String t = sharedViewModel.getCurrentTitle().getValue();
        tvTitle.setText(t != null ? t : "Đơn hàng");
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
      if (navController.getCurrentDestination() != null
        && navController.getCurrentDestination().getId() != R.id.navigation_home) {
        navController.navigate(R.id.navigation_home, null, navOptions);
      }
    });
    
    binding.myCustomMenu.navigationDashboard.setOnClickListener(v -> {
      if (navController.getCurrentDestination() != null
        && navController.getCurrentDestination().getId() != R.id.navigation_dashboard) {
        navController.navigate(R.id.navigation_dashboard, null, navOptions);
      }
    });
    
    binding.myCustomMenu.navigationNegotiation.setOnClickListener(v -> {
      if (navController.getCurrentDestination() != null
        && navController.getCurrentDestination().getId() != R.id.navigation_negotiation) {
        navController.navigate(R.id.navigation_negotiation, null, navOptions);
      }
    });
    
    binding.myCustomMenu.navigationProfile.setOnClickListener(v -> {
      if (navController.getCurrentDestination() != null
        && navController.getCurrentDestination().getId() != R.id.navigation_profile) {
        navController.navigate(R.id.navigation_profile, null, navOptions);
      }
    });
  }
  
  // --- Các hàm xử lý chung khi click icon ---
  private void openCartScreen() {
    Toast.makeText(this, "Mở Giỏ hàng", Toast.LENGTH_SHORT).show();
  }
  private void openChatScreen() {
    Toast.makeText(this, "Mở Chat", Toast.LENGTH_SHORT).show();
  }
  private void openNotificationScreen() {
    Toast.makeText(this, "Mở Thông báo", Toast.LENGTH_SHORT).show();
  }
  
}
