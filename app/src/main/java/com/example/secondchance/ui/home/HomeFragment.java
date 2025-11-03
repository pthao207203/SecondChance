package com.example.secondchance.ui.home;

import android.os.Bundle;
import android.util.Log; // Thêm Log để kiểm tra
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;     // 👈 THÊM IMPORT NÀY
import androidx.navigation.Navigation;   // 👈 THÊM IMPORT NÀY
import com.example.secondchance.R;
import com.google.android.material.card.MaterialCardView; // 👈 THÊM IMPORT NÀY

public class HomeFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    // 1. Tìm MaterialCardView "Thương lượng" bằng ID bạn đã thêm ở Bước 3.1
    MaterialCardView cardNegotiation = view.findViewById(R.id.card_negotiation);

    // 2. Gán sự kiện click cho nó
    if (cardNegotiation != null) {
      cardNegotiation.setOnClickListener(v -> {
        try {
          // 3. Tìm NavController từ View
          NavController navController = Navigation.findNavController(v);

          // 4. Điều hướng bằng Action đã định nghĩa ở Bước 3.3
          navController.navigate(R.id.action_homeFragment_to_negotiationFragment);

        } catch (Exception e) {
          // In lỗi nếu không thể điều hướng (ví dụ: action sai tên)
          Log.e("HomeFragment", "Lỗi điều hướng: ", e);
        }
      });
    } else {
      // Cảnh báo nếu không tìm thấy ID
      Log.w("HomeFragment", "Không tìm thấy View với ID: card_negotiation");
    }
  }
}