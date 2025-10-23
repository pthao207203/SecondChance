package com.example.secondchance;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.secondchance.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
  private ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    Log.d("MainActivityDebug", "MainActivity onCreate called with activity_main.xml");

    // Xử lý click cho các mục menu
    LinearLayout navHome = findViewById(R.id.nav_home);
    LinearLayout navAi = findViewById(R.id.nav_ai);
    LinearLayout navNegotiate = findViewById(R.id.nav_negotiate);
    LinearLayout navMe = findViewById(R.id.nav_me);

    if (navHome != null) {
      navHome.setOnClickListener(v -> Toast.makeText(this, "Trang chủ", Toast.LENGTH_SHORT).show());
    }
    if (navAi != null) {
      navAi.setOnClickListener(v -> Toast.makeText(this, "AI định giá", Toast.LENGTH_SHORT).show());
    }
    if (navNegotiate != null) {
      navNegotiate.setOnClickListener(v -> Toast.makeText(this, "Thương lượng", Toast.LENGTH_SHORT).show());
    }
    if (navMe != null) {
      navMe.setOnClickListener(v -> Toast.makeText(this, "Tôi", Toast.LENGTH_SHORT).show());
    }
  }
}
