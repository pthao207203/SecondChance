package com.example.secondchance.ui.negotiation;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.request.NegotiationCreateRequest;
import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.NegotiationListResponse;
import com.example.secondchance.util.LogApiError;
import com.google.android.material.button.MaterialButton;

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

public class NegotiationCancelledFragment extends Fragment {
    
    private RecyclerView recyclerView;
    private NegotiationCancelledAdapter adapter;
    private final List<NegotiationCancelled> cancelledList = new ArrayList<>();
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_negotiation_cancelled, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewNegotiationCancelled);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new NegotiationCancelledAdapter(
          cancelledList,
          this::showNegotiationDialog   // callback khi bấm "Tiếp tục thương lượng"
        );
        recyclerView.setAdapter(adapter);
        
        loadCancelledNegotiationsFromApi();
        
        return view;
    }
    private void showNegotiationDialog(NegotiationCancelled item) {
        if (!isAdded()) return;
        
        Dialog inputDialog = new Dialog(requireContext());
        inputDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        inputDialog.setContentView(R.layout.dialog_input_negotiation);
        inputDialog.setCancelable(true);
        
        TextView tvOriginalPrice = inputDialog.findViewById(R.id.tvOriginalPrice);
        EditText etPrice = inputDialog.findViewById(R.id.etNegotiationPrice);
        EditText etReason = inputDialog.findViewById(R.id.etReason);
        MaterialButton btnSend = inputDialog.findViewById(R.id.btnRegisterSeller);
        ImageView btnClose = inputDialog.findViewById(R.id.btnCloseSuccess);
        
        // Parse price "₫ 50.000" -> 50000
        double originalPrice = 0;
        try {
            String clean = item.getPrice()
              .replace("₫", "")
              .replace(".", "")
              .replace(",", "")
              .replace(" ", "");
            originalPrice = Double.parseDouble(clean);
        } catch (Exception e) {
            // fallback
            originalPrice = 0;
        }
        
        tvOriginalPrice.setText(String.format("%,.0f", originalPrice));
        
        int suggested = (int) (originalPrice * 0.8);
        if (originalPrice > 0) {
            etPrice.setText(String.valueOf(suggested));
            etPrice.setSelection(etPrice.getText().length());
        }
        
        etPrice.requestFocus();
        if (inputDialog.getWindow() != null) {
            inputDialog.getWindow().setSoftInputMode(
              WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
            );
        }
        
        btnClose.setOnClickListener(v -> inputDialog.dismiss());
        
        double finalOriginalPrice = originalPrice;
        btnSend.setOnClickListener(v -> {
            String inputPrice = etPrice.getText().toString().trim();
            String reason = etReason.getText().toString().trim();
            
            if (inputPrice.isEmpty() || reason.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng điền đầy đủ!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double offerPrice;
            try {
                offerPrice = Double.parseDouble(inputPrice);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (finalOriginalPrice > 0 && offerPrice >= finalOriginalPrice) {
                Toast.makeText(requireContext(), "Giá phải nhỏ hơn giá gốc!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            inputDialog.dismiss();
            
            NegotiationCreateRequest body =
              new NegotiationCreateRequest((long) offerPrice, reason);
            
            RetrofitProvider.product()
              .createNegotiation(item.getId(), body)   // nhớ thêm getProductId() vào NegotiationCancelled nếu chưa có
              .enqueue(new Callback<BasicResponse>() {
                  @Override
                  public void onResponse(
                    @NonNull Call<BasicResponse> call,
                    @NonNull Response<BasicResponse> response
                  ) {
                      if (!isAdded()) return;
                      
                      if (response.isSuccessful()
                        && response.body() != null
                        && response.body().success) {
                          inputDialog.dismiss();
                          showSuccessDialog();
                      } else {
                          Toast.makeText(requireContext(),
                            "Gửi yêu cầu thương lượng thất bại",
                            Toast.LENGTH_SHORT).show();
                          LogApiError.log("NegotiationCancelledFragment", "createNegotiation", response);
                      }
                  }
                  
                  @Override
                  public void onFailure(
                    @NonNull Call<BasicResponse> call,
                    @NonNull Throwable t
                  ) {
                      if (!isAdded()) return;
                      Toast.makeText(requireContext(),
                        "Lỗi mạng, không gửi được yêu cầu",
                        Toast.LENGTH_SHORT).show();
                      LogApiError.logFailure("NegotiationCancelledFragment", "createNegotiation", t);
                  }
              });
            
            showSuccessDialog();
        });
        
        inputDialog.show();
    }
    
    private void showSuccessDialog() {
        if (!isAdded()) return;
        
        Dialog successDialog = new Dialog(requireContext());
        successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        successDialog.setContentView(R.layout.dialog_negotiation_send);
        successDialog.setCancelable(false);
        
        MaterialButton btnNextTime = successDialog.findViewById(R.id.btnNextTime);
        MaterialButton btnSeeNow = successDialog.findViewById(R.id.btnSeeNow);
        
        btnNextTime.setOnClickListener(v -> successDialog.dismiss());
        
        // Đang ở tab Thương lượng rồi nên "Xem ngay" chỉ cần đóng dialog
        btnSeeNow.setOnClickListener(v -> {
            successDialog.dismiss();
            Fragment parent = getParentFragment();
            if (parent instanceof NegotiationFragment) {
                ((NegotiationFragment) parent).openSentTab();
            }
        });
    }
    
    private void loadCancelledNegotiationsFromApi() {
        RetrofitProvider.product()
          .getNegotiations("cancelled,rejected", 1, 20)   // status=cancelled
          .enqueue(new Callback<NegotiationListResponse>() {
              @Override
              public void onResponse(@NonNull Call<NegotiationListResponse> call,
                                     @NonNull Response<NegotiationListResponse> response) {
                  if (!isAdded()) return;
                  
                  if (response.isSuccessful()
                    && response.body() != null
                    && response.body().success) {
                      
                      List<NegotiationCancelled> uiList = mapToUiModel(response.body());
                      cancelledList.clear();
                      cancelledList.addAll(uiList);
                      adapter.notifyDataSetChanged();
                  } else {
                      Toast.makeText(requireContext(),
                        "Không tải được danh sách thương lượng bị hủy",
                        Toast.LENGTH_SHORT).show();
                      LogApiError.log("NegotiationCancelledFragment", "getNegotiations(cancelled)", response);
                  }
              }
              
              @Override
              public void onFailure(@NonNull Call<NegotiationListResponse> call,
                                    @NonNull Throwable t) {
                  if (!isAdded()) return;
                  Toast.makeText(requireContext(),
                    "Lỗi kết nối khi tải thương lượng bị hủy",
                    Toast.LENGTH_SHORT).show();
                  LogApiError.logFailure("NegotiationCancelledFragment", "getNegotiations(cancelled)", t);
              }
          });
    }
    
    private List<NegotiationCancelled> mapToUiModel(NegotiationListResponse res) {
        List<NegotiationCancelled> list = new ArrayList<>();
        if (res == null || res.data == null || res.data.items == null) return list;
        
        for (NegotiationListResponse.Item item : res.data.items) {
            NegotiationCancelled ui = new NegotiationCancelled();
            
            ui.setId(item.productId);
            // Gán status: "cancel" / "reject"
            ui.setStatus(item.status);
            
            if (item.counterpart != null) {
                ui.setShopName(item.counterpart.name);
                ui.setShopAvatarUrl(item.counterpart.avatar);
                ui.setShopId(item.counterpart.id);
            }
            if (item.currentUser != null) {
                ui.setUserName(item.currentUser.name);
                ui.setUserAvatarUrl(item.currentUser.avatar);
            }
            
            ui.setNegotiationRound("Thương lượng lần " + item.attemptNumber);
            ui.setTitle(item.productName);
            ui.setPrice(formatCurrencyNumber(item.offeredPrice));
            ui.setQuantity("x" + item.quantity);
            ui.setFixedPriceText("Giá cố định");
            
            ui.setCreatedDate("Đã tạo ngày: " + formatDate(item.createdAt));
            ui.setProductDate(formatDate(item.createdAt));
            ui.setShopDate(formatDate(item.acceptedAt)); // hoặc cancelledAt / rejectedAt nếu có
            
            // Set message tuỳ status
            if (ui.isCancel()) {
                ui.setReplyMessage(
                  "Thanh toán quá hạn trong vòng 24 giờ.\n" +
                    "Giao dịch đã kết thúc và bạn không thể tiếp tục giao dịch."
                );
            } else if (ui.isReject()) {
                ui.setReplyMessage(
                  "Cảm ơn bạn đã ra giá, nhưng shop thấy giá bạn đưa ra không phù hợp, " +
                    "mong bạn thông cảm và có thể ra giá khác."
                );
            } else {
                // fallback nếu BE trả ra status khác
                ui.setReplyMessage("Giao dịch đã kết thúc.");
            }
            
            list.add(ui);
        }
        
        return list;
    }
    
    
    private String formatCurrencyNumber(long value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(value);
    }
    
    private String formatDate(String iso) {
        if (iso == null) return "";
        try {
            SimpleDateFormat isoFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            isoFmt.setLenient(false);
            Date date = isoFmt.parse(iso);
            SimpleDateFormat out = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
            return out.format(date);
        } catch (ParseException e) {
            return "";
        }
    }
}
