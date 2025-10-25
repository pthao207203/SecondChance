package com.example.secondchance;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secondchance.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;
  private NavController navController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    Log.d("MainActivityDebug", "MainActivity onCreate called");

    // Lấy NavController từ NavHostFragment
    NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.fragment_container);
    if (navHostFragment != null) {
      navController = navHostFragment.getNavController();
    }

    // Setup bottom navigation
    binding.menu.navHome.setOnClickListener(v -> {
      if (navController != null) {
        navController.navigate(R.id.navigation_home);
      }
    });

    binding.menu.navAi.setOnClickListener(v ->
            Toast.makeText(this, "AI định giá", Toast.LENGTH_SHORT).show()
    );

    binding.menu.navNegotiate.setOnClickListener(v ->
            Toast.makeText(this, "Thương lượng", Toast.LENGTH_SHORT).show()
    );

    binding.menu.navMe.setOnClickListener(v -> {
      if (navController != null) {
        navController.navigate(R.id.navigation_profile);
      }
    });
  }
}