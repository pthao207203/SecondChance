package com.example.secondchance.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.databinding.ItemCategoryChipBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {
  
  public interface OnCategoryClickListener {
    void onCategoryClick(HomeApi.Category category);
  }
  
  private final List<HomeApi.Category> items = new ArrayList<>();
  private OnCategoryClickListener listener;
  
  // id category đang chọn (null = không lọc theo category)
  private String selectedCategoryId = null;
  
  public void setOnCategoryClickListener(OnCategoryClickListener l) {
    this.listener = l;
  }
  
  public void setSelectedCategoryId(String id) {
    this.selectedCategoryId = id;
    notifyDataSetChanged();
  }
  
  public String getSelectedCategoryId() {
    return selectedCategoryId;
  }
  
  static class VH extends RecyclerView.ViewHolder {
    final ItemCategoryChipBinding b;
    VH(ItemCategoryChipBinding binding) {
      super(binding.getRoot());
      this.b = binding;
    }
  }
  
  @NonNull
  @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inf = LayoutInflater.from(parent.getContext());
    ItemCategoryChipBinding b = ItemCategoryChipBinding.inflate(inf, parent, false);
    return new VH(b);
  }
  
  @Override
  public void onBindViewHolder(@NonNull VH h, int pos) {
    HomeApi.Category c = items.get(pos);
    if (c.icon != null) c.icon = c.icon.replace('-', '_');
    h.b.setItem(c);
    h.b.executePendingBindings();
    
    // category đang chọn?
    boolean isSelected = (selectedCategoryId != null && selectedCategoryId.equals(c.id));
    
    int bgColor = ContextCompat.getColor(
      h.itemView.getContext(),
      isSelected ? R.color.lightActiveDay : R.color.lightDay
    );
    h.b.ivCategory.setCardBackgroundColor(bgColor);
    
    h.itemView.setOnClickListener(v -> {
      // 👉 Nếu click lại đúng category đang chọn → huỷ chọn (bỏ filter category)
      if (selectedCategoryId != null && selectedCategoryId.equals(c.id)) {
        selectedCategoryId = null;
      } else {
        // 👉 Chọn category mới
        selectedCategoryId = c.id;
      }
      
      notifyDataSetChanged();
      
      if (listener != null) {
        listener.onCategoryClick(c);
      }
    });
  }
  
  @Override
  public int getItemCount() {
    return items.size();
  }
  
  public void submit(List<HomeApi.Category> data) {
    items.clear();
    if (data != null) items.addAll(data);
    notifyDataSetChanged();
  }
}
