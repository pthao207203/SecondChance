package com.example.secondchance.ui.profile.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.secondchance.data.repo.WalletRepository;
import com.example.secondchance.ui.profile.WalletTopupHistory;

import java.time.ZoneId;
import java.util.List;

public class WalletHistoryViewModel extends ViewModel {
  
  private final WalletRepository repo = new WalletRepository(ZoneId.systemDefault());
  
  private final MutableLiveData<Params> params = new MutableLiveData<>();
  public LiveData<WalletRepository.Result<List<WalletTopupHistory>>> result =
    Transformations.switchMap(params, p ->
      repo.loadHistory(p.startUtc, p.endUtc, p.backendSupportsRange)
    );
  
  public void load(long startUtc, long endUtc, boolean backendSupportsRange) {
    params.setValue(new Params(startUtc, endUtc, backendSupportsRange));
  }
  
  private static class Params {
    final long startUtc, endUtc;
    final boolean backendSupportsRange;
    Params(long s, long e, boolean r) { startUtc = s; endUtc = e; backendSupportsRange = r; }
  }
}
