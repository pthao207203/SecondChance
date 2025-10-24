package com.example.secondchance.ui.home; // Hoặc package tương ứng của bạn

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.secondchance.R;

public class HomeFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    // Tải (inflate) layout cho fragment này
    return inflater.inflate(R.layout.fragment_home, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    // Tại đây, bạn có thể thêm logic cho HomeFragment,
    // ví dụ: findViewById, thiết lập listener cho các nút bên trong fragment...
    // ví dụ: TextView xemTatCa = view.findViewById(R.id.id_cua_nut_xem_tat_ca);
  }
}
