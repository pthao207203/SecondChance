package com.example.secondchance.ui.card;

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

public class ImageUrlSliderAdapter extends RecyclerView.Adapter<ImageUrlSliderAdapter.VH> {
  
  private final List<String> urls = new ArrayList<>();
  
  public void setData(List<String> list) {
    urls.clear();
    if (list != null) urls.addAll(list);
    notifyDataSetChanged();
  }
  
  @NonNull @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_slider_image, parent, false);
    return new VH(v);
  }
  
  @Override public void onBindViewHolder(@NonNull VH h, int pos) {
    String url = urls.get(pos);
    Glide.with(h.image)
      .load(url)
      .centerCrop()
      .into(h.image);
  }
  
  @Override public int getItemCount() { return urls.size(); }
  
  static class VH extends RecyclerView.ViewHolder {
    ImageView image;
    VH(@NonNull View itemView) {
      super(itemView);
      image = itemView.findViewById(R.id.sliderImage);
    }
  }
}
