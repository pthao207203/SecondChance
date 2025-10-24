package com.example.secondchance;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.secondchance.databinding.ActivityMainBinding;
import com.example.secondchance.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    Log.d("MainActivityDebug", "MainActivity onCreate called");

    if (savedInstanceState == null) {
      loadFragment(new HomeFragment(), false);
    }

    binding.menu.navHome.setOnClickListener(v -> loadFragment(new HomeFragment(), false));
    binding.menu.navAi.setOnClickListener(v -> Toast.makeText(this, "AI định giá", Toast.LENGTH_SHORT).show());
    binding.menu.navNegotiate.setOnClickListener(v -> Toast.makeText(this, "Thương lượng", Toast.LENGTH_SHORT).show());

  }

  private void loadFragment(Fragment fragment, boolean addToBackStack) {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(binding.fragmentContainer.getId(), fragment);
    if (addToBackStack) {
      fragmentTransaction.addToBackStack(null);
    }
    fragmentTransaction.commit();
  }
}
