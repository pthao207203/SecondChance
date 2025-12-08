package com.example.secondchance.ui.negotiation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.response.NegotiationListResponse;
import com.example.secondchance.util.LogApiError;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NegotiationCompletedFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private NegotiationCompletedAdapter adapter;
    private final List<NegotiationCompleted> completedList = new ArrayList<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_negotiation_completed, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewNegotiationCompleted);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new NegotiationCompletedAdapter(completedList);
        recyclerView.setAdapter(adapter);
        
        loadCompletedNegotiationsFromApi();
        
        return view;
    }
    
    private void loadCompletedNegotiationsFromApi() {
        // status = "purchased"
        RetrofitProvider.product()
          .getNegotiations("purchased", 1, 20)
          .enqueue(new Callback<NegotiationListResponse>() {
              @Override
              public void onResponse(@NonNull Call<NegotiationListResponse> call,
                                     @NonNull Response<NegotiationListResponse> response) {
                  if (!isAdded()) return;
                  
                  if (response.isSuccessful()
                    && response.body() != null
                    && response.body().success) {
                      
                      List<NegotiationCompleted> uiList = mapToUiModel(response.body());
                      
                      completedList.clear();
                      completedList.addAll(uiList);
                      adapter.notifyDataSetChanged();
                  } else {
                      Toast.makeText(requireContext(),
                        "Không tải được danh sách thương lượng đã mua",
                        Toast.LENGTH_SHORT).show();
                      LogApiError.log("NegotiationCompletedFragment",
                        "getNegotiations(completed)", response);
                  }
              }
              
              @Override
              public void onFailure(@NonNull Call<NegotiationListResponse> call,
                                    @NonNull Throwable t) {
                  if (!isAdded()) return;
                  Toast.makeText(requireContext(),
                    "Lỗi kết nối khi tải danh sách đã mua",
                    Toast.LENGTH_SHORT).show();
                  LogApiError.logFailure("NegotiationCompletedFragment",
                    "getNegotiations(completed)", t);
              }
          });
    }
    
    private List<NegotiationCompleted> mapToUiModel(NegotiationListResponse res) {
        List<NegotiationCompleted> list = new ArrayList<>();
        if (res == null || res.data == null || res.data.items == null) return list;
        
        for (NegotiationListResponse.Item item : res.data.items) {
            NegotiationCompleted ui = new NegotiationCompleted();
            
            // ===== HEADER: tên user + ngày =====
            if (item.currentUser != null) {
                ui.setUserName(item.currentUser.name);
                ui.setUserAvatarUrl(item.currentUser.avatar);
            } else {
                ui.setUserName("Người dùng");
            }
            
            // Ngày hoàn tất (acceptedAt / completedAt / createdAt – tuỳ BE)
            String completedDate = item.acceptedAt != null ? item.acceptedAt : item.createdAt;
            ui.setProductDate(formatDate(completedDate));
            
            // Thương lượng lần N
            ui.setNegotiationRound("Thương lượng lần " + item.attemptNumber);
            
            // ===== SHOP / NHẮN TIN =====
            if (item.counterpart != null) {
                ui.setShopName(item.counterpart.name);
                ui.setShopAvatarUrl(item.counterpart.avatar);
                ui.setShopId(item.counterpart.id);
            }
            
            // ===== SẢN PHẨM =====
            ui.setTitle(item.productName);
            ui.setProductImageUrl(item.productImage);
            ui.setPrice(formatCurrencyNumber(item.offeredPrice));
            ui.setQuantity("x" + item.quantity);
            
            ui.setFixedPriceText("Giá cố định");
            ui.setCreatedDate("Đã tạo ngày: " + formatDate(item.createdAt));
            
            // ===== MESSAGE TRẠNG THÁI =====
            ui.setReplyMessage(
              "Đơn hàng đã được thanh toán thành công. " +
                "Bạn có thể xem chi tiết trong mục Lịch sử đơn hàng."
            );
            
            list.add(ui);
        }
        
        return list;
    }
    
    private String formatCurrencyNumber(long value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(value);   // ví dụ: 7.000.000
    }
    
    private String formatDate(String iso) {
        if (iso == null) return "";
        try {
            SimpleDateFormat isoFmt =
              new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            isoFmt.setLenient(false);
            Date date = isoFmt.parse(iso);
            SimpleDateFormat out =
              new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
            return out.format(date);
        } catch (ParseException e) {
            return "";
        }
    }
}
