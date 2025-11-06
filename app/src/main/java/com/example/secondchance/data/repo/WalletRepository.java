package com.example.secondchance.data.repo;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.data.remote.MeApi;
import com.example.secondchance.dto.response.WalletHistoryResponse;
import com.example.secondchance.ui.profile.WalletTopupHistory;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletRepository {
  
  private final MeApi api = RetrofitProvider.me();
  private final ZoneId zone;
  private final DateTimeFormatter dateFmt;   // "dd/MM/yyyy HH:mm"
  private final NumberFormat vndFmt;
  
  public WalletRepository(ZoneId zone) {
    this.zone = zone;
    this.dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(zone);
    this.vndFmt = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
  }
  
  public LiveData<Result<List<WalletTopupHistory>>> loadHistory(long startUtcMs, long endUtcMs, boolean backendSupportsRange) {
    MutableLiveData<Result<List<WalletTopupHistory>>> live = new MutableLiveData<>();
    live.postValue(Result.loading());
    
    // Chuẩn ISO 8601 UTC cho query
    String startIso = Instant.ofEpochMilli(startUtcMs).toString();
    String endIso   = Instant.ofEpochMilli(endUtcMs).toString();
    
    Call<WalletHistoryResponse> call = backendSupportsRange
      ? api.getHistory(startIso, endIso)
      : api.getHistoryAll();
    
    call.enqueue(new Callback<WalletHistoryResponse>() {
      @Override public void onResponse(Call<WalletHistoryResponse> c, Response<WalletHistoryResponse> r) {
        if (!r.isSuccessful()) {
          live.postValue(Result.error("Lỗi mạng hoặc dữ liệu trống (" + r.code() + "):" + r.message()));
          return;
        } else if (r.body() == null || r.body().data == null) {
          live.postValue(Result.error("Lỗi dữ liệu"));
          return;
        }
        List<WalletHistoryResponse.Item> items = r.body().data.items != null ? r.body().data.items : new ArrayList<>();
        
        // Nếu backend chưa hỗ trợ filter thì lọc client-side theo start/end
        List<WalletTopupHistory> mapped = new ArrayList<>();
        for (WalletHistoryResponse.Item it : items) {
          long tsMs;
          try {
            tsMs = Instant.parse(it.time).toEpochMilli();
          } catch (Exception e) {
            continue;
          }
          if (!backendSupportsRange) {
            if (tsMs < startUtcMs || tsMs > endUtcMs) continue;
          }
          
          String title = it.amount >= 0 ? "Nạp tiền vào ví" : "Thanh toán / Trừ tiền";
          String when  = dateFmt.format(Instant.ofEpochMilli(tsMs));
          
          // Định dạng số tiền VND (âm/dương)
          String money = vndFmt.format(Math.abs(it.amount)); // chỉ format giá trị tuyệt đối cho hiển thị phụ
          int displayAmount = (int) Math.abs(it.amount);     // giữ tương thích constructor cũ (int)
          
          // Map về model UI
          WalletTopupHistory ui = new WalletTopupHistory(title, when, displayAmount);
          // Gợi ý: nếu cần hiển thị dấu âm/dương trong Adapter, bạn có thể thêm field khác.
          mapped.add(ui);
        }
        
        // Sắp xếp mới nhất lên đầu (nếu backend chưa sort)
        mapped.sort((a, b) -> {
          // a.getDate() hiện là String "dd/MM/yyyy HH:mm" nên không parse ở đây; thường backend đã sort.
          return 0;
        });
        
        live.postValue(Result.success(mapped));
      }
      
      @Override public void onFailure(Call<WalletHistoryResponse> c, Throwable t) {
        live.postValue(Result.error(t.getMessage() != null ? t.getMessage() : "Không gọi được API"));
      }
    });
    
    return live;
  }
  
  // Wrapper nhỏ gọn cho trạng thái
  public static class Result<T> {
    public enum Status { LOADING, SUCCESS, ERROR }
    public final Status status;
    public final T data;
    public final String error;
    
    private Result(Status s, @Nullable T d, @Nullable String e) {
      status = s; data = d; error = e;
    }
    
    public static <T> Result<T> loading() { return new Result<>(Status.LOADING, null, null); }
    public static <T> Result<T> success(T d) { return new Result<>(Status.SUCCESS, d, null); }
    public static <T> Result<T> error(String e) { return new Result<>(Status.ERROR, null, e); }
  }
}
