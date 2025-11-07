package com.example.secondchance.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.repo.WalletRepository;
import com.example.secondchance.ui.profile.WalletHistoryAdapter;
import com.example.secondchance.ui.profile.WalletTopupHistory;
import com.example.secondchance.ui.profile.viewmodel.WalletHistoryViewModel;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WalletHistoryFragment extends Fragment {
  
  private LinearLayout btnDateFilter;
  private TextView tvDateRange;
  
  @Nullable private Long currentStartUtc = null;
  @Nullable private Long currentEndUtc   = null;
  
  private final ZoneId zone = ZoneId.systemDefault();
  private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  
  private WalletHistoryViewModel viewModel;
  private WalletHistoryAdapter adapter; // dùng lại adapter hiện có
  private RecyclerView rv;
  
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
    rv = v.findViewById(R.id.rvHistory);
    
    // Default 30 ngày gần nhất
    long todayUtc = MaterialDatePicker.todayInUtcMilliseconds();
    long thirtyDaysMs = 29L * 24 * 60 * 60 * 1000;
    currentStartUtc = todayUtc - thirtyDaysMs;
    currentEndUtc   = todayUtc;
    
    tvDateRange.setText(toRangeText(currentStartUtc, currentEndUtc));
    
    // RecyclerView
    rv.setLayoutManager(new LinearLayoutManager(requireContext()));
    adapter = new WalletHistoryAdapter(new ArrayList<>());
    rv.setAdapter(adapter);
    rv.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
    
    // ViewModel
    viewModel = new ViewModelProvider(this).get(WalletHistoryViewModel.class);
    viewModel.result.observe(getViewLifecycleOwner(), res -> {
      if (res == null) return;
      Gson gson = new Gson();
      String json = gson.toJson(res);
      Log.d("WalletHistory", json);
      
      if (res.status == WalletRepository.Result.Status.LOADING) {
        // TODO: show progress nếu cần
      } else if (res.status == WalletRepository.Result.Status.SUCCESS) {
        adapter.setData(res.data != null ? res.data : new ArrayList<>());
      } else if (res.status == WalletRepository.Result.Status.ERROR) {
         Toast.makeText(requireContext(), res.error, Toast.LENGTH_SHORT).show();
      }
    });
    
    // Load lần đầu
    reloadHistory(currentStartUtc, currentEndUtc);
    
    // Date range picker
    btnDateFilter.setOnClickListener(view -> openDateRangePicker());
  }
  
  private void openDateRangePicker() {
    CalendarConstraints.Builder constraints = new CalendarConstraints.Builder()
      .setEnd(MaterialDatePicker.todayInUtcMilliseconds());
    
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
      
      currentStartUtc = startUtc;
      currentEndUtc   = endUtc;
      
      tvDateRange.setText(toRangeText(startUtc, endUtc));
      reloadHistory(startUtc, endUtc);
    });
    
    picker.show(getParentFragmentManager(), "range");
  }
  
  private String toRangeText(long startUtc, long endUtc) {
    LocalDate s = Instant.ofEpochMilli(startUtc).atZone(zone).toLocalDate();
    LocalDate e = Instant.ofEpochMilli(endUtc).atZone(zone).toLocalDate();
    return fmt.format(s) + " - " + fmt.format(e);
  }
  
  private void reloadHistory(long startUtc, long endUtc) {
    // Nếu backend đã hỗ trợ query ?start=&end= -> đặt true
    boolean backendSupportsRange = true; // đổi sang false nếu server chưa hỗ trợ
    viewModel.load(startUtc, endUtc, backendSupportsRange);
  }
}
