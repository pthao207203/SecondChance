package com.example.secondchance.ui.message;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.MessageApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.dto.request.MessageSendRequestDto;
import com.example.secondchance.dto.response.MessageListResponseDto;
import com.example.secondchance.dto.response.MessageSendResponseDto;
import com.example.secondchance.util.CloudinaryUploader;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageDetailFragment extends Fragment {
    
    private static final String ARG_PARTNER_ID = "partner_id";
    private static final String ARG_PARTNER_NAME = "partner_name";
    private static final String ARG_PARTNER_AVATAR = "partner_avatar";
    
    private String partnerId;
    private String partnerName;
    private String partnerAvatar;
    
    private ImageView imgToolbarAvatar;
    private TextView tvToolbarTitle;
    private RecyclerView rvMessages;
    private EditText edtMessage;
    private ImageButton btnSend;
    private ImageButton imgIcon;
    
    private View imagePreviewContainer;
    private ImageView imgPreview;
    private ImageButton btnRemoveImage;
    
    private Uri selectedImageUri = null;
    
    private MessageAdapter messageAdapter;
    private final List<MessageListResponseDto.MessageItemDto> messages = new ArrayList<>();
    
    private final Handler autoRefreshHandler = new Handler(Looper.getMainLooper());
    private Runnable autoRefreshRunnable;
    
    private ActivityResultLauncher<String> imagePickerLauncher;
    
    private boolean isSending = false;
    
    public static MessageDetailFragment newInstance(String partnerId,
                                                    String partnerName,
                                                    String partnerAvatar) {
        MessageDetailFragment f = new MessageDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARTNER_ID, partnerId);
        args.putString(ARG_PARTNER_NAME, partnerName);
        args.putString(ARG_PARTNER_AVATAR, partnerAvatar);
        f.setArguments(args);
        return f;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle args = getArguments();
        if (args != null) {
            // Lưu ý: tham số đi qua NavController dùng key "partnerId", "partnerName", "partnerAvatar"
            partnerId = args.getString("partnerId");
            partnerName = args.getString("partnerName");
            partnerAvatar = args.getString("partnerAvatar");
        }
        
        // Chọn ảnh từ gallery
        imagePickerLauncher = registerForActivityResult(
          new ActivityResultContracts.GetContent(),
          uri -> {
              if (uri != null) {
                  selectedImageUri = uri;
                  
                  // Hiện preview
                  if (imagePreviewContainer != null) {
                      imagePreviewContainer.setVisibility(View.VISIBLE);
                  }
                  if (imgPreview != null) {
                      Glide.with(this)
                        .load(uri)
                        .into(imgPreview);
                  }
                  
                  // Tô màu icon cho dễ nhận biết
                  if (imgIcon != null) {
                      imgIcon.setColorFilter(ContextCompat.getColor(
                        requireContext(), R.color.darkerDay
                      ));
                  }
              }
          }
        );
        
        // Auto refresh 5s/lần
        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchMessages(1, 20);
                autoRefreshHandler.postDelayed(this, 5000);
            }
        };
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViews(view);
        setupToolbar(view);
        setupRecyclerView();
        setupSendButton();
        
        imgIcon.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        
        btnRemoveImage.setOnClickListener(v -> clearSelectedImage());
        
        fetchMessages(1, 20);
    }
    
    private void setupViews(View view) {
        imgToolbarAvatar = view.findViewById(R.id.imgToolbarAvatar);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);
        rvMessages = view.findViewById(R.id.rvMessages);
        edtMessage = view.findViewById(R.id.edtMessage);
        btnSend = view.findViewById(R.id.btnSend);
        imgIcon = view.findViewById(R.id.imgicon);
        
        imagePreviewContainer = view.findViewById(R.id.imagePreviewContainer);
        imgPreview = view.findViewById(R.id.imgPreview);
        btnRemoveImage = view.findViewById(R.id.btnRemoveImage);
    }
    
    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });
        
        tvToolbarTitle.setText(partnerName);
        
        Glide.with(this)
          .load(partnerAvatar)
          .placeholder(R.drawable.avatar1)
          .error(R.drawable.avatar1)
          .into(imgToolbarAvatar);
    }
    
    private void setupRecyclerView() {
        LinearLayoutManager lm = new LinearLayoutManager(getContext());
        lm.setStackFromEnd(true);      // cuộn xuống cuối
        rvMessages.setLayoutManager(lm);
        
        messageAdapter = new MessageAdapter(messages, partnerId, partnerAvatar);
        rvMessages.setAdapter(messageAdapter);
    }
    
    private void setupSendButton() {
        btnSend.setOnClickListener(v -> {
            if (isSending) {
                // đang gửi rồi, không cho spam
                return;
            }
            
            String text = edtMessage.getText().toString().trim();
            
            // Không có gì để gửi
            if (TextUtils.isEmpty(text) && selectedImageUri == null) {
                return;
            }
            
            isSending = true;
            btnSend.setEnabled(false);
            btnSend.setAlpha(0.4f);
            
            final String textToSend = text;
            
            // Clear text ở input ngay khi user bấm gửi
            edtMessage.setText("");
            
            // Nếu có cả ảnh lẫn text: gửi ảnh trước, xong gửi text
            if (selectedImageUri != null && !TextUtils.isEmpty(textToSend)) {
                sendImageThenText(selectedImageUri, textToSend);
            } else if (selectedImageUri != null) {
                // Chỉ ảnh
                sendImageMessage(selectedImageUri);
            } else {
                // Chỉ text
                sendTextMessage(textToSend);
            }
        });
    }
    
    private void clearSelectedImage() {
        selectedImageUri = null;
        if (imagePreviewContainer != null) {
            imagePreviewContainer.setVisibility(View.GONE);
        }
        if (imgIcon != null) {
            imgIcon.clearColorFilter();
        }
    }
    
    private void resetSendingState() {
        isSending = false;
        if (btnSend != null) {
            btnSend.setEnabled(true);
            btnSend.setAlpha(1f);
        }
    }
    
    // ====================== GỬI TEXT ======================
    
    private void sendTextMessage(String textToSend) {
        MessageApi api = RetrofitProvider.message();
        
        MessageSendRequestDto body = new MessageSendRequestDto(
          partnerId,   // receiverId
          "text",      // contentType
          textToSend
        );
        
        api.sendMessage(body).enqueue(new Callback<MessageSendResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<MessageSendResponseDto> call,
                                   @NonNull Response<MessageSendResponseDto> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    // Gửi thất bại → trả lại text cho user
                    edtMessage.setText(textToSend);
                    Toast.makeText(getContext(), "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
                    resetSendingState();
                    return;
                }
                
                // Thành công: text đã được clear từ trước
                fetchMessages(1, 20);
                resetSendingState();
            }
            
            @Override
            public void onFailure(@NonNull Call<MessageSendResponseDto> call,
                                  @NonNull Throwable t) {
                // Lỗi mạng → trả lại text
                edtMessage.setText(textToSend);
                Toast.makeText(getContext(), "Không gửi được tin nhắn", Toast.LENGTH_SHORT).show();
                resetSendingState();
            }
        });
    }
    
    // ====================== GỬI ẢNH ======================
    
    private void sendImageMessage(Uri imageUri) {
        new Thread(() -> {
            try {
                List<Uri> uriList = new ArrayList<>();
                uriList.add(imageUri);
                
                // Upload lên Cloudinary, folder "messages"
                List<String> mediaUrls = CloudinaryUploader.uploadImages(
                  requireContext(),
                  uriList,
                  "messages",
                  RetrofitProvider.cloudinary()
                );
                
                if (mediaUrls == null || mediaUrls.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                          "Upload ảnh thất bại",
                          Toast.LENGTH_LONG).show();
                        resetSendingState();
                    });
                    return;
                }
                
                String imageUrl = mediaUrls.get(0);
                
                // Gửi message image lên BE (call sync)
                MessageSendRequestDto body = new MessageSendRequestDto(
                  partnerId,
                  "image",
                  imageUrl
                );
                
                retrofit2.Response<MessageSendResponseDto> res =
                  RetrofitProvider.message().sendMessage(body).execute();
                
                if (res.isSuccessful() && res.body() != null && res.body().isSuccess()) {
                    requireActivity().runOnUiThread(() -> {
                        // clear preview + icon
                        clearSelectedImage();
                        fetchMessages(1, 20);
                        resetSendingState();
                    });
                } else {
                    String err = res.errorBody() != null ? res.errorBody().string() : "Unknown error";
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                          "Gửi ảnh thất bại: " + err,
                          Toast.LENGTH_LONG).show();
                        resetSendingState();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                      "Lỗi gửi ảnh: " + e.getMessage(),
                      Toast.LENGTH_LONG).show();
                    resetSendingState();
                });
            }
        }).start();
    }
    
    // ============ GỬI ẢNH + TEXT (ảnh trước, text sau) ============
    
    private void sendImageThenText(Uri imageUri, String textToSend) {
        new Thread(() -> {
            try {
                List<Uri> uriList = new ArrayList<>();
                uriList.add(imageUri);
                
                List<String> mediaUrls = CloudinaryUploader.uploadImages(
                  requireContext(),
                  uriList,
                  "messages",
                  RetrofitProvider.cloudinary()
                );
                
                if (mediaUrls == null || mediaUrls.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                          "Upload ảnh thất bại",
                          Toast.LENGTH_LONG).show();
                        // text chưa gửi → trả lại text
                        edtMessage.setText(textToSend);
                        resetSendingState();
                    });
                    return;
                }
                
                String imageUrl = mediaUrls.get(0);
                
                // 1) gửi message ảnh
                MessageSendRequestDto imgBody = new MessageSendRequestDto(
                  partnerId,
                  "image",
                  imageUrl
                );
                retrofit2.Response<MessageSendResponseDto> imgRes =
                  RetrofitProvider.message().sendMessage(imgBody).execute();
                
                if (!(imgRes.isSuccessful() && imgRes.body() != null && imgRes.body().isSuccess())) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                          "Gửi ảnh thất bại",
                          Toast.LENGTH_LONG).show();
                        // text chưa gửi → trả lại vào ô
                        edtMessage.setText(textToSend);
                        resetSendingState();
                    });
                    return;
                }
                
                // 2) nếu ảnh ok -> gửi tiếp text
                MessageSendRequestDto textBody = new MessageSendRequestDto(
                  partnerId,
                  "text",
                  textToSend
                );
                retrofit2.Response<MessageSendResponseDto> textRes =
                  RetrofitProvider.message().sendMessage(textBody).execute();
                
                if (textRes.isSuccessful() && textRes.body() != null && textRes.body().isSuccess()) {
                    requireActivity().runOnUiThread(() -> {
                        clearSelectedImage();
                        // edtMessage đã rỗng từ trước
                        fetchMessages(1, 20);
                        resetSendingState();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),
                          "Gửi text sau ảnh thất bại",
                          Toast.LENGTH_LONG).show();
                        // text thất bại → cho user nhập lại
                        edtMessage.setText(textToSend);
                        resetSendingState();
                    });
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                      "Lỗi gửi ảnh + text: " + e.getMessage(),
                      Toast.LENGTH_LONG).show();
                    // trả lại text
                    edtMessage.setText(textToSend);
                    resetSendingState();
                });
            }
        }).start();
    }
    
    // (Hàm mapSendResponseToListItem nếu chưa dùng thì bạn có thể xoá đi)
    private MessageListResponseDto.MessageItemDto mapSendResponseToListItem(
      MessageSendResponseDto.MessageData dataRes
    ) {
        MessageListResponseDto.MessageItemDto item = new MessageListResponseDto.MessageItemDto();
        item.setId(dataRes.getId());
        item.setSenderId(dataRes.getSenderId());
        item.setContentType(dataRes.getContentType());
        item.setContent(dataRes.getContent());
        item.setAttachments(dataRes.getAttachments());
        item.setStatus(dataRes.getStatus());
        item.setSentAt(dataRes.getSentAt());
        item.setReadAt(dataRes.getReadAt());
        return item;
    }
    
    // ====================== LOAD DANH SÁCH TIN NHẮN ======================
    
    private void fetchMessages(int page, int pageSize) {
        MessageApi api = RetrofitProvider.message();
        api.getMessagesWith(partnerId, page, pageSize)
          .enqueue(new Callback<MessageListResponseDto>() {
              @Override
              public void onResponse(@NonNull Call<MessageListResponseDto> call,
                                     @NonNull Response<MessageListResponseDto> response) {
                  if (!response.isSuccessful()) {
                      Toast.makeText(getContext(),
                        "Lỗi tải tin nhắn " + response.code(),
                        Toast.LENGTH_SHORT).show();
                      return;
                  }
                  
                  MessageListResponseDto body = response.body();
                  if (body == null || !body.isSuccess()
                    || body.getData() == null
                    || body.getData().getItems() == null) {
                      return;
                  }
                  
                  messages.clear();
                  messages.addAll(body.getData().getItems());
                  messageAdapter.notifyDataSetChanged();
                  
                  if (!messages.isEmpty()) {
                      rvMessages.scrollToPosition(messages.size() - 1);
                  }
              }
              
              @Override
              public void onFailure(@NonNull Call<MessageListResponseDto> call,
                                    @NonNull Throwable t) {
                  Toast.makeText(getContext(),
                    "Không tải được tin nhắn",
                    Toast.LENGTH_SHORT).show();
              }
          });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        autoRefreshHandler.postDelayed(autoRefreshRunnable, 5000);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }
}
