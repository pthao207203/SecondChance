package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.ui.profile.WalletHistoryAdapter;
import com.example.secondchance.ui.profile.WalletTopupHistory;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WalletHistoryFragment extends Fragment {
  
  private LinearLayout btnDateFilter;
  private TextView tvDateRange;
  
  // giữ selection hiện tại (millis UTC) để mở picker lần sau
  @Nullable private Long currentStartUtc = null;
  @Nullable private Long currentEndUtc   = null;
  
  private final ZoneId zone = ZoneId.systemDefault();
  private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  
  
  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_wallet_history, container, false);
  }
  
  @Override
  public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(v, savedInstanceState);
    
    btnDateFilter = v.findViewById(R.id.btnDateFilter);
    tvDateRange   = v.findViewById(R.id.tvDateRange);
    
    long todayUtc = MaterialDatePicker.todayInUtcMilliseconds();
    long thirtyDaysMs = 29L * 24 * 60 * 60 * 1000; // 29 ngày trước + hôm nay = 30 ngày
    currentStartUtc = todayUtc - thirtyDaysMs;
    currentEndUtc   = todayUtc;
    
    tvDateRange.setText(toRangeText(currentStartUtc, currentEndUtc));
    reloadHistory(currentStartUtc, currentEndUtc);
    
    // demo: bạn thay bằng data từ API
    List<WalletTopupHistory> demo = new ArrayList<>();
    demo.add(new WalletTopupHistory("Nạp tiền vào ví", "17/06/2025", 170000));
    demo.add(new WalletTopupHistory("Nạp tiền vào ví", "17/06/2025", 170000));
    demo.add(new WalletTopupHistory("Nạp tiền vào ví", "17/06/2025", 170000));
    demo.add(new WalletTopupHistory("Nạp tiền vào ví", "17/06/2025", 170000));
    
    RecyclerView rv = v.findViewById(R.id.rvHistory);
    rv.setLayoutManager(new LinearLayoutManager(requireContext()));
    rv.setAdapter(new WalletHistoryAdapter(demo));
    
    // Divider mảnh giống mock
    DividerItemDecoration dec = new DividerItemDecoration(requireContext(),
      DividerItemDecoration.VERTICAL);
    rv.addItemDecoration(dec);
    
    // filter ngày (chỉ demo click)
    LinearLayout btnDateFilter = v.findViewById(R.id.btnDateFilter);
    TextView tvDateRange = v.findViewById(R.id.tvDateRange);
    btnDateFilter.setOnClickListener(view -> openDateRangePicker());
  }
  private void openDateRangePicker() {
    // Chặn chọn ngày trong tương lai
    CalendarConstraints.Builder constraints = new CalendarConstraints.Builder()
      .setEnd(MaterialDatePicker.todayInUtcMilliseconds());
    
    // Dùng selection lần trước nếu có, để UX tốt hơn
    Pair<Long, Long> initialSelection = (currentStartUtc != null && currentEndUtc != null)
      ? Pair.create(currentStartUtc, currentEndUtc)
      : null;
    
    MaterialDatePicker<Pair<Long, Long>> picker =
      MaterialDatePicker.Builder.dateRangePicker()
        .setTitleText("Chọn khoảng ngày")
        .setCalendarConstraints(constraints.build())
        .setSelection(initialSelection)
        .setTheme(R.style.ThemeOverlay_SecondChance_DatePicker)
        .build();
    
    picker.addOnPositiveButtonClickListener(selection -> {
      if (selection == null) return;
      Long startUtc = selection.first;
      Long endUtc   = selection.second;
      if (startUtc == null || endUtc == null) return;
      
      // Lưu lại để mở picker lần sau
      currentStartUtc = startUtc;
      currentEndUtc   = endUtc;
      
      // Cập nhật UI
      tvDateRange.setText(toRangeText(startUtc, endUtc));
      
      // Tải lại dữ liệu theo khoảng ngày
      reloadHistory(startUtc, endUtc);
    });
    
    picker.addOnNegativeButtonClickListener(dialog -> {
      // Người dùng bấm Cancel -> không làm gì
    });
    
    picker.addOnDismissListener(dialog -> {
      // Đóng picker -> không làm gì thêm
    });
    
    picker.show(getParentFragmentManager(), "range");
  }
  
  private String toRangeText(long startUtc, long endUtc) {
    LocalDate s = Instant.ofEpochMilli(startUtc).atZone(zone).toLocalDate();
    LocalDate e = Instant.ofEpochMilli(endUtc).atZone(zone).toLocalDate();
    return fmt.format(s) + " - " + fmt.format(e);
  }
  
  /** TODO: thay bằng logic gọi API + cập nhật adapter */
  private void reloadHistory(long startUtc, long endUtc) {
    // Ví dụ:
    // viewModel.loadTopupHistory(startUtc, endUtc);
    // adapter.submitList(...);
  }
}
