package com.example.secondchance.ui.order;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.databinding.FragmentBoughtOrderRateBinding;
import com.example.secondchance.dto.response.OrderDetailResponse;
import com.example.secondchance.util.CloudinaryUploader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RateOrderFragment extends Fragment {

    private static final String TAG = "RateOrderFragment";
    private FragmentBoughtOrderRateBinding binding;
    private String orderId;
    private int currentRating = 0;

    private final List<Uri> selectedImages = new ArrayList<>();
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupImagePicker();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBoughtOrderRateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }

        setupRatingStars();
        setupImageUploadUI();
        
        if (orderId != null) {
            fetchOrderDetails();
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            binding.tvDate.setText(sdf.format(new Date()));
        }

        binding.btnSubmitRate.setOnClickListener(v -> submitRating());
    }

    private void fetchOrderDetails() {
        RetrofitProvider.order().getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    populateUI(response.body().data);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to fetch order details", t);
                if (getContext() != null) {
                     Toast.makeText(getContext(), "Không thể tải thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void populateUI(OrderDetailResponse.Data data) {
        if (getContext() == null) return;

        if (data.seller != null) {
            binding.tvShopName.setText(data.seller.userName);
            Glide.with(this)
                 .load(data.seller.userAvatar)
                 .placeholder(R.drawable.ic_profile0)
                 .into(binding.ivShopAvatar);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.tvDate.setText(sdf.format(new Date()));

        if (data.order != null && data.order.orderItems != null && !data.order.orderItems.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (OrderDetailResponse.OrderItem item : data.order.orderItems) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(item.name).append(", số lượng ").append(item.qty).append(".");
            }
            binding.tvProductInfo.setText(sb.toString());
        }
    }

    private void setupRatingStars() {
        binding.star1.setOnClickListener(v -> updateRating(1));
        binding.star2.setOnClickListener(v -> updateRating(2));
        binding.star3.setOnClickListener(v -> updateRating(3));
        binding.star4.setOnClickListener(v -> updateRating(4));
        binding.star5.setOnClickListener(v -> updateRating(5));
    }

    private void updateRating(int rating) {
        currentRating = rating;
        
        updateStarIcon(binding.star1, rating >= 1);
        updateStarIcon(binding.star2, rating >= 2);
        updateStarIcon(binding.star3, rating >= 3);
        updateStarIcon(binding.star4, rating >= 4);
        updateStarIcon(binding.star5, rating >= 5);
    }

    private void updateStarIcon(ImageView star, boolean isSelected) {
        star.setImageResource(isSelected ? R.drawable.star : R.drawable.starempty);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && selectedImages.size() < 5) {
                        selectedImages.add(uri);
                        updateThumbnails();
                    } else if (uri != null) {
                        Toast.makeText(getContext(), "Bạn chỉ được chọn tối đa 5 ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupImageUploadUI() {
        binding.btnAddImage.setOnClickListener(v -> {
             if (selectedImages.size() < 5) {
                imagePickerLauncher.launch("image/*");
            } else {
                Toast.makeText(getContext(), "Bạn chỉ được chọn tối đa 5 ảnh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateThumbnails() {
        binding.thumbnailContainer.removeAllViews();
        binding.thumbnailContainer.addView(binding.btnAddImage);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Uri uri : selectedImages) {
            View thumbnailView = inflater.inflate(R.layout.item_thumbnail, binding.thumbnailContainer, false);
            ImageView thumbnail = thumbnailView.findViewById(R.id.img_thumb);
            thumbnail.setImageURI(uri);
            
            thumbnailView.setOnClickListener(v -> {
                selectedImages.remove(uri);
                updateThumbnails();
            });

            binding.thumbnailContainer.addView(thumbnailView);
        }
    }

    private void submitRating() {
        if (currentRating == 0) {
            Toast.makeText(getContext(), "Vui lòng chọn đánh giá sao", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnSubmitRate.setEnabled(false);
        binding.btnSubmitRate.setText("Đang gửi...");

        // Upload ảnh lên Cloudinary ở background thread
        new Thread(() -> {
            List<String> uploadedUrls = new ArrayList<>();
            try {
                if (!selectedImages.isEmpty()) {
                    uploadedUrls = CloudinaryUploader.uploadImages(
                            requireContext(),
                            selectedImages,
                            "orders/rating", // Folder lưu ảnh đánh giá
                            RetrofitProvider.cloudinary()
                    );
                }
                
                // Sau khi upload xong, quay lại main thread để gọi API
                List<String> finalMedia = uploadedUrls;
                requireActivity().runOnUiThread(() -> sendRatingApi(finalMedia));

            } catch (IOException e) {
                Log.e(TAG, "Upload failed", e);
                requireActivity().runOnUiThread(() -> {
                    if (getContext() != null) {
                         Toast.makeText(getContext(), "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    binding.btnSubmitRate.setEnabled(true);
                    binding.btnSubmitRate.setText("Gửi");
                });
            }
        }).start();
    }

    private void sendRatingApi(List<String> mediaUrls) {
        String description = binding.etDescription.getText().toString();
        OrderApi.RateOrderRequest request = new OrderApi.RateOrderRequest(currentRating, description, mediaUrls);

        if (orderId != null) {
            RetrofitProvider.order().rateOrder(orderId, request).enqueue(new Callback<OrderApi.RateOrderResponse>() {
                @Override
                public void onResponse(@NonNull Call<OrderApi.RateOrderResponse> call, @NonNull Response<OrderApi.RateOrderResponse> response) {
                    if (binding == null) return;
                    binding.btnSubmitRate.setEnabled(true);
                    binding.btnSubmitRate.setText("Gửi");
                    
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        Toast.makeText(getContext(), "Đánh giá thành công!", Toast.LENGTH_SHORT).show();
                        navigateToComment();
                    } else {
                        Toast.makeText(getContext(), "Đánh giá thất bại", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<OrderApi.RateOrderResponse> call, @NonNull Throwable t) {
                    if (binding == null) return;
                    binding.btnSubmitRate.setEnabled(true);
                    binding.btnSubmitRate.setText("Gửi");
                    Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            if (binding != null) {
                binding.btnSubmitRate.setEnabled(true);
                binding.btnSubmitRate.setText("Gửi");
            }
            Toast.makeText(getContext(), "Lỗi: Không có OrderID", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToComment() {
         try {
            Navigation.findNavController(requireView()).navigate(R.id.action_rateOrderFragment_to_commentFragment);
        } catch (Exception e) {
             Log.e(TAG, "Navigation failed", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
