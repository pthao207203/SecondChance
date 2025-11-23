package com.example.secondchance.ui.chatbot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.BuildConfig;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.RetrofitClient;
import com.example.secondchance.dto.request.GeminiRequest;
import com.example.secondchance.dto.response.GeminiResponse;
import com.google.android.material.button.MaterialButton;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatAiFragment extends Fragment {
  
  private RecyclerView recyclerView;
  private EditText edtMessage;
  private ImageView btnSend, btnPickImage;
  private LinearLayout emptyStateLayout;
  
  // Các view cho Preview ảnh
  private CardView previewLayout;
  private ImageView imgPreview, btnClosePreview;
  
  private MaterialButton btnXinChao, btnHuongDanGia, btnDinhGiaMonDo, btnHoanTien;
  
  private ChatAdapter chatAdapter;
  private List<MessageModel> messageList;
  private Bitmap selectedBitmap; // Lưu ảnh đang chọn
  
  // --- 1. KHỞI TẠO LAUNCHER ĐỂ CHỌN ẢNH ---
  private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
      if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
        Uri imageUri = result.getData().getData();
        try {
          // Chuyển Uri thành Bitmap
          selectedBitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
          // Resize ảnh nhỏ lại để gửi nhanh hơn (quan trọng)
          selectedBitmap = resizeBitmap(selectedBitmap, 1024);
          
          showPreviewImage(selectedBitmap);
        } catch (Exception e) {
          Toast.makeText(getContext(), "Lỗi chọn ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
      }
    });
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_chat, container, false);
    
    // Ánh xạ View
    recyclerView = view.findViewById(R.id.chatRecyclerView);
    edtMessage = view.findViewById(R.id.edtMessage);
    btnSend = view.findViewById(R.id.btnSend);
    btnPickImage = view.findViewById(R.id.btnPickImage);
    emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
    
    // Ánh xạ Preview
    previewLayout = view.findViewById(R.id.previewLayout);
    imgPreview = view.findViewById(R.id.imgPreview);
    btnClosePreview = view.findViewById(R.id.btnClosePreview);
    
    btnXinChao = view.findViewById(R.id.btnXinChao);
    btnHuongDanGia = view.findViewById(R.id.btnHuongDanGia);
    btnDinhGiaMonDo = view.findViewById(R.id.btnDinhGiaMonDo);
    btnHoanTien = view.findViewById(R.id.btnHoanTien);
    
    messageList = new ArrayList<>();
    chatAdapter = new ChatAdapter(messageList);
    
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    layoutManager.setStackFromEnd(true);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(chatAdapter);
    
    setupUI();
    setupSuggestionButtons();
    
    return view;
  }
  
  private void setupUI() {
    // Gửi tin nhắn
    btnSend.setOnClickListener(v -> {
      String message = edtMessage.getText().toString().trim();
      
      // Cho phép gửi nếu có chữ HOẶC có ảnh
      if (!message.isEmpty() || selectedBitmap != null) {
        showChatView();
        
        // 1. Hiển thị tin nhắn lên màn hình (Text + Ảnh)
        addUserMessage(message, selectedBitmap);
        
        // 2. Gửi lên API
        sendMessageToGemini(message, selectedBitmap);
        
        // 3. Reset UI
        edtMessage.setText("");
        clearPreviewImage();
      }
    });
    
    // Chọn ảnh
    btnPickImage.setOnClickListener(v -> {
      Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      pickImageLauncher.launch(intent);
    });
    
    // Xóa ảnh đang chọn
    btnClosePreview.setOnClickListener(v -> clearPreviewImage());
  }
  
  // --- 2. HÀM GỬI API (TEXT + ẢNH) ---
  private void sendMessageToGemini(String userMessage, Bitmap image) {
    List<GeminiRequest.Part> parts = new ArrayList<>();
    
    // Nếu có text thì thêm vào
    if (!userMessage.isEmpty()) {
      parts.add(new GeminiRequest.Part(userMessage));
    }
    
    // Nếu có ảnh thì convert sang Base64 và thêm vào
    if (image != null) {
      String base64String = convertBitmapToBase64(image);
      GeminiRequest.InlineData inlineData = new GeminiRequest.InlineData("image/jpeg", base64String);
      parts.add(new GeminiRequest.Part(inlineData.toString()));
    }
    
    GeminiRequest.Content content = new GeminiRequest.Content("user", parts);
    GeminiRequest request = new GeminiRequest(Collections.singletonList(content));
    
    // Gọi API
    RetrofitClient.getGeminiApi().generateContent(BuildConfig.GEMINI_API_KEY, request)
      .enqueue(new Callback<GeminiResponse>() {
        @Override
        public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
          if (response.isSuccessful() && response.body() != null) {
            try {
              String botReply = response.body().candidates.get(0).content.parts.get(0).text;
              addBotMessage(botReply);
            } catch (Exception e) {
              addBotMessage("Lỗi đọc dữ liệu AI.");
            }
          } else {
            try {
              String err = response.errorBody().string();
              System.out.println("API Error: " + err);
              addBotMessage("Lỗi API: " + response.code());
            } catch (Exception e) {}
          }
        }
        
        @Override
        public void onFailure(Call<GeminiResponse> call, Throwable t) {
          addBotMessage("Lỗi mạng: " + t.getMessage());
        }
      });
  }
  
  // --- CÁC HÀM HỖ TRỢ UI ---
  
  private void showPreviewImage(Bitmap bitmap) {
    previewLayout.setVisibility(View.VISIBLE);
    imgPreview.setImageBitmap(bitmap);
  }
  
  private void clearPreviewImage() {
    selectedBitmap = null;
    previewLayout.setVisibility(View.GONE);
    imgPreview.setImageDrawable(null);
  }
  
  // Hàm chuyển Bitmap -> Base64 String
  private String convertBitmapToBase64(Bitmap bitmap) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream); // Nén 80% chất lượng
    return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
  }
  
  // Hàm resize ảnh (để tránh gửi ảnh quá nặng gây lỗi)
  private Bitmap resizeBitmap(Bitmap image, int maxSize) {
    int width = image.getWidth();
    int height = image.getHeight();
    
    float bitmapRatio = (float) width / (float) height;
    if (bitmapRatio > 1) {
      width = maxSize;
      height = (int) (width / bitmapRatio);
    } else {
      height = maxSize;
      width = (int) (height * bitmapRatio);
    }
    return Bitmap.createScaledBitmap(image, width, height, true);
  }
  
  // ... Các hàm setupSuggestionButtons, showChatView, addUserMessage, addBotMessage giữ nguyên ...
  // Chỉ lưu ý hàm addUserMessage cần hỗ trợ nhận bitmap (đã làm ở các bài trước)
  
  private void setupSuggestionButtons() {
    View.OnClickListener suggestionListener = v -> {
      MaterialButton btn = (MaterialButton) v;
      String text = btn.getText().toString();
      showChatView();
      addUserMessage(text, null);
      sendMessageToGemini(text, null);
    };
    
    btnXinChao.setOnClickListener(suggestionListener);
    btnHuongDanGia.setOnClickListener(suggestionListener);
    btnDinhGiaMonDo.setOnClickListener(suggestionListener);
    btnHoanTien.setOnClickListener(suggestionListener);
  }
  
  private void showChatView() {
    if (emptyStateLayout.getVisibility() == View.VISIBLE) {
      emptyStateLayout.setVisibility(View.GONE);
      recyclerView.setVisibility(View.VISIBLE);
    }
  }
  
  private void addUserMessage(String message, Bitmap img) {
    messageList.add(new MessageModel(message, true, img));
    chatAdapter.notifyItemInserted(messageList.size() - 1);
    recyclerView.scrollToPosition(messageList.size() - 1);
  }
  
  private void addBotMessage(String message) {
    if (!isAdded()) return;
    requireActivity().runOnUiThread(() -> {
      messageList.add(new MessageModel(message, false));
      chatAdapter.notifyItemInserted(messageList.size() - 1);
      recyclerView.smoothScrollToPosition(messageList.size() - 1);
    });
  }
}