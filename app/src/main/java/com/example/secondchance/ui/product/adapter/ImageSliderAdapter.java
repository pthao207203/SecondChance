package com.example.secondchance.ui.product.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {
    
    private List<String> imageUrls = new ArrayList<>();
    
    public ImageSliderAdapter(List<String> imageUrls) {
        if (imageUrls != null) {
            this.imageUrls = imageUrls;
        }
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = (imageUrls != null) ? imageUrls : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        holder.bind(imageUrl);
    }
    
    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }
    
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }
        
        public void bind(String imageUrl) {
            if (TextUtils.isEmpty(imageUrl)) {
                imageView.setImageResource(R.drawable.ic_launcher_background); // hoặc placeholder riêng
                return;
            }
            
            try {
                // nếu BE gửi resource id dạng "2131230897" thì vẫn dùng được
                int resId = Integer.parseInt(imageUrl);
                imageView.setImageResource(resId);
            } catch (NumberFormatException e) {
                // còn lại là URL → dùng Glide
                Glide.with(imageView.getContext())
                  .load(imageUrl)
                  .placeholder(R.drawable.ic_launcher_background) // đổi sang placeholder bạn muốn
                  .error(R.drawable.ic_launcher_background)
                  .into(imageView);
            }
        }
    }
}
