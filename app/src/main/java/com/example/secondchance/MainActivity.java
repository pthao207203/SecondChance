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
import androidx.navigation.Navigation;
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

    NavHostFragment navHostFragment =
      (NavHostFragment) getSupportFragmentManager()
        .findFragmentById(R.id.nav_host_fragment_activity_main);
    if (navHostFragment == null) {
      Log.e("MainActivity", "NavHostFragment is null!");
      return;
    }
    navController = navHostFragment.getNavController();

    boolean forceLogout = getIntent() != null && getIntent().getBooleanExtra("forceLogout", false);
    if (forceLogout) {
      navController.setGraph(R.navigation.nav_auth);
      Log.d("MainActivity", "Force logout detected -> setGraph(nav_auth)");
    } else {

      navController.setGraph(R.navigation.mobile_navigation);
      Log.d("MainActivity", "Normal launch -> setGraph(mobile_navigation)");
    }

    binding.myCustomMenu.navigationHome.setOnClickListener(v -> {
      NavController c = navController;
      c.navigate(R.id.navigation_home);
    });

    sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
    sharedViewModel.getCurrentTitle().observe(this, this::applySharedTitleIfNeeded);
    
    setupIconClickListeners();
    setupBottomMenuClickListeners();

    navController.addOnDestinationChangedListener((controller, destination, args) -> {
      Log.d("MainActivity", "Destination changed: " + destination.getId() + " label=" + destination.getLabel());
      updateUiVisibility(destination);
    });

    NavDestination cur = navController.getCurrentDestination();
    if (cur != null) updateUiVisibility(cur);
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
//    tabsAppBar.setVisibility(inOrder && !inAuth ? View.VISIBLE : View.GONE);
    tabsAppBar.setVisibility(View.GONE);
    
    if (!inAuth) {
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

  private void setupIconClickListeners() {
    binding.headerMain.iconCart.setOnClickListener(v -> openCartScreen());
    binding.headerMain.iconChat.setOnClickListener(v -> openChatScreen());
    binding.headerMain.iconNotify.setOnClickListener(v -> openNotificationScreen());
    binding.headerMain.iconSearch.setOnClickListener(v -> {
      // 1. Lấy text trong ô search
      String keyword = "";
      if (binding.headerMain.searchBar != null) { // đổi tên cho trùng id của bạn
        keyword = binding.headerMain.searchBar.getText().toString().trim();
      }
      
      if (keyword.isEmpty()) {
        Toast.makeText(this, "Vui lòng nhập từ khoá tìm kiếm", Toast.LENGTH_SHORT).show();
        return;
      }
      
      // 2. Đóng gói Bundle gửi sang AuctionFilterFragment
      Bundle bundle = new Bundle();
      bundle.putString("searchName", keyword);  // 🔹 phải trùng KEY_SEARCH_NAME
      
      // 3. Điều hướng sang AuctionFilterFragment
      NavController navController =
        Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
      
      // tuỳ nav_graph của bạn: có thể là action_global_auctionFilterFragment hoặc action_home_to_auctionFilterFragment
      navController.navigate(R.id.action_home_to_auctionFilterFragment, bundle);
    });
  }

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
        && navController.getCurrentDestination().getId() != R.id.navigation_ai_chatbot) {
        navController.navigate(R.id.navigation_ai_chatbot, null, navOptions);
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

  private void openCartScreen() {
    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.cartFragment) {
        navController.navigate(R.id.action_global_to_cartFragment);
    }
  }
  private void openChatScreen() {
    if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.messageFragment) {
      navController.navigate(R.id.action_global_to_messageFragment);
    }
  }
  private void openNotificationScreen() {
    Toast.makeText(this, "Mở Thông báo", Toast.LENGTH_SHORT).show();
  }
}
