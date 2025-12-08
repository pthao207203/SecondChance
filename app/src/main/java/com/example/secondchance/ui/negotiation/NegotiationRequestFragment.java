// ui/negotiation/NegotiationRequestFragment.java
package com.example.secondchance.ui.negotiation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.ProductApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentRecyclerNegotiationRequestBinding;
import com.example.secondchance.dto.response.NegotiationListResponse;
import com.example.secondchance.util.LogApiError;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NegotiationRequestFragment extends Fragment {
    
    private FragmentRecyclerNegotiationRequestBinding binding;
    private NegotiationRequestAdapter adapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerNegotiationRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();
        setupChatClick();
        loadRequestsFromApi();
    }
    
    private void setupRecyclerView() {
        adapter = new NegotiationRequestAdapter();
        binding.recyclerViewRequests.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewRequests.setHasFixedSize(true);
        binding.recyclerViewRequests.setAdapter(adapter);
    }
    
    private void setupChatClick() {
        adapter.setOnChatClickListener(item -> {
            if (item.getShopId() == null || item.getShopId().isEmpty()) return;
            
            Bundle args = new Bundle();
            args.putString("partnerId", item.getShopId());
            args.putString("partnerName", item.getShopName());
            args.putString("partnerAvatar", item.getShopAvatarUrl());
            
            NavHostFragment.findNavController(this)
              .navigate(R.id.action_trade_to_messageDetail, args);
        });
    }
    
    private void loadRequestsFromApi() {
        ProductApi api = RetrofitProvider.product();
        
        // status "pending": tuỳ BE của bạn, đổi lại nếu khác
        api.getNegotiations("pending", 1, 20)
          .enqueue(new Callback<NegotiationListResponse>() {
              @Override
              public void onResponse(@NonNull Call<NegotiationListResponse> call,
                                     @NonNull Response<NegotiationListResponse> response) {
                  if (!response.isSuccessful()) {
                      if (getContext() != null) {
                          Toast.makeText(getContext(),
                            "Lỗi tải thương lượng: " + response.code(),
                            Toast.LENGTH_SHORT).show();
                      }
                      LogApiError.log("NegotiationRequestFragment",
                        "getNegotiations(requested)", response);
                      return;
                  }
                  Gson gson = new Gson();
                  Log.d("NegotiationRequestFragment", "getNegotiations(requested) failed" + gson.toJson(response.body()));
                  NegotiationListResponse body = response.body();
                  List<NegotiationRequest> uiList = mapToUiList(body);
                  adapter.submitList(uiList);
              }
              
              @Override
              public void onFailure(@NonNull Call<NegotiationListResponse> call,
                                    @NonNull Throwable t) {
                  if (getContext() != null) {
                      Toast.makeText(getContext(),
                        "Không tải được thương lượng",
                        Toast.LENGTH_SHORT).show();
                  }
                  LogApiError.logFailure("NegotiationRequestFragment",
                    "getNegotiations(requested)", t);
              }
          });
    }
    
    private List<NegotiationRequest> mapToUiList(NegotiationListResponse body) {
        List<NegotiationRequest> result = new ArrayList<>();
        
        for (NegotiationListResponse.Item item : body.data.items) {
            
            NegotiationRequest ui = new NegotiationRequest();
            
            // Header: tên bạn, ngày tạo (simple)
            ui.setUserName("Bạn");  // hoặc lấy từ profile user hiện tại
            ui.setDate(formatDateShort(item.createdAt));
            
            // Text "Thương lượng lần X"
            ui.setNegotiationText("Thương lượng lần " + item.attemptNumber);
            
            // Sản phẩm
            ui.setProductTitle(item.productName);
            ui.setPrice(formatPrice(item.offeredPrice));
            ui.setQuantity(String.valueOf(item.quantity));
            ui.setCreatedDate("Đã tạo ngày: " + formatDateShort(item.createdAt));
            ui.setProductImageUrl(item.productImage);
            
            // Avatar user (nếu có lưu trong local) – tạm để null / placeholder
            ui.setUserAvatarUrl(item.currentUser.avatar);
            
            // Thông tin shop (counterpart từ BE)
            if (item.currentUser != null) {
                ui.setUserName(item.currentUser.name);
                ui.setUserAvatarUrl(item.currentUser.avatar);
            }
            if (item.counterpart != null) {
                ui.setShopId(item.counterpart.id);
                ui.setShopName(item.counterpart.name);
                ui.setShopAvatarUrl(item.counterpart.avatar);
            }
            
            // hasReply: tuỳ logic BE (ví dụ: đã accepted/declined thì true)
            // Với tab "đã gửi" chưa được phản hồi thì có thể luôn false
            ui.setHasReply(false);
            
            result.add(ui);
        }
        
        return result;
    }
    
    private String formatPrice(long price) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return "₫ " + nf.format(price);
    }
    
    private String formatDateShort(String isoDateTime) {
        // TODO: parse ISO "2025-10-25T06:21:36.176Z" thành "25/10/2025"
        // Tạm dùng substring cho nhanh, bạn có thể đổi sang DateTimeFormatter
        try {
            String datePart = isoDateTime.substring(0, 10); // 2025-10-25
            String[] parts = datePart.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch (Exception ignored) {}
        return isoDateTime;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
