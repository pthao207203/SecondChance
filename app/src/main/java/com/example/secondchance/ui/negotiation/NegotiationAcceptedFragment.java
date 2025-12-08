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

import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentRecyclerNegotiationAcceptedBinding;
import com.example.secondchance.dto.response.NegotiationListResponse;
import com.example.secondchance.util.LogApiError;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NegotiationAcceptedFragment extends Fragment {
    
    private FragmentRecyclerNegotiationAcceptedBinding binding;
    private NegotiationAcceptedAdapter adapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerNegotiationAcceptedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupRecyclerView();
        loadAcceptedNegotiationsFromApi();
    }
    
    private void setupRecyclerView() {
        adapter = new NegotiationAcceptedAdapter();
        binding.recyclerViewAccepted.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewAccepted.setHasFixedSize(true);
        binding.recyclerViewAccepted.setAdapter(adapter);
    }
    
    private void loadAcceptedNegotiationsFromApi() {
        // TODO: nếu bạn có loading view thì show ở đây
        RetrofitProvider.product()
          .getNegotiations("accepted", 1, 20)
          .enqueue(new Callback<NegotiationListResponse>() {
              @Override
              public void onResponse(
                Call<NegotiationListResponse> call,
                Response<NegotiationListResponse> response
              ) {
                  // TODO: hide loading
                  if (!isAdded()) return;
                  
                  if (response.isSuccessful() && response.body() != null && response.body().success) {
                      List<NegotiationAccepted> uiList = mapToUiModel(response.body());
                      adapter.submitList(uiList);
                  } else {
                      LogApiError.log("NegotiationAccepted", "getAcceptedNegotiations", response);
                      Toast.makeText(requireContext(), "Không tải được danh sách thương lượng", Toast.LENGTH_SHORT).show();
                  }
              }
              
              @Override
              public void onFailure(
                Call<NegotiationListResponse> call,
                Throwable t
              ) {
                  // TODO: hide loading
                  if (!isAdded()) return;
                  Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
              }
          });
    }
    
    private List<NegotiationAccepted> mapToUiModel(NegotiationListResponse res) {
        List<NegotiationAccepted> list = new ArrayList<>();
        if (res == null || res.data == null || res.data.items == null) return list;
        
        for (NegotiationListResponse.Item item : res.data.items) {
            NegotiationAccepted ui = new NegotiationAccepted();
            
            // 👉 map id
            ui.setId(item.productId);        // tên field theo BE của bạn
            ui.setNegotiationId(item.id);           // ví dụ: id của lần thương lượng
            
            if (item.counterpart != null) {
                ui.setShopName(item.counterpart.name);
                ui.setShopAvatarUrl(item.counterpart.avatar);
                ui.setShopId(item.counterpart.id);
            }
            if (item.currentUser != null) {
                ui.setUserName(item.currentUser.name);
                ui.setUserAvatarUrl(item.currentUser.avatar);
            }
            
            ui.setNegotiationText("Thương lượng lần " + item.attemptNumber);
            ui.setProductTitle(item.productName);
            ui.setProductImageUrl(item.productImage);
            ui.setPrice(formatCurrencyNumber(item.offeredPrice));
            ui.setQuantity(item.quantity);
            ui.setCreatedDate(formatDate(item.createdAt));
            ui.setDate(formatDate(item.acceptedAt));
            ui.setReplyDate(formatDate(item.acceptedAt));
            
            ui.setReplyMessage(
              "Vui lòng thanh toán trong vòng 24h kể từ khi yêu cầu được chấp nhận. " +
                "Nếu không thanh toán, đơn hàng sẽ tự động hủy."
            );
            
            ui.setPaid(false); // chưa thanh toán
            
            list.add(ui);
        }
        
        return list;
    }
    
    private String formatCurrencyNumber(long value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(value); // ví dụ: 7.000.000
    }
    
    private String formatDate(String iso) {
        if (iso == null) return "";
        try {
            // "2025-10-25T06:21:36.176Z"
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            isoFmt.setLenient(false);
            Date date = isoFmt.parse(iso);
            SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
            return out.format(date);
        } catch (ParseException e) {
            return "";
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
