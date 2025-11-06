package com.example.secondchance.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.data.remote.HomeApi;
import com.example.secondchance.databinding.ItemCategoryChipBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {
  private final List<HomeApi.Category> items = new ArrayList<>();
  
  static class VH extends RecyclerView.ViewHolder {
    final ItemCategoryChipBinding b;
    VH(ItemCategoryChipBinding binding) {
      super(binding.getRoot());
      this.b = binding;
    }
  }
  
  @NonNull @Override
  public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inf = LayoutInflater.from(parent.getContext());
    ItemCategoryChipBinding b = ItemCategoryChipBinding.inflate(inf, parent, false);
    return new VH(b);
  }
  
  @Override
  public void onBindViewHolder(@NonNull VH h, int pos) {
    HomeApi.Category c = items.get(pos);
    // chuẩn hóa icon nếu backend dùng dấu '-'
    if (c.icon != null) c.icon = c.icon.replace('-', '_');
    h.b.setItem(c);
    h.b.executePendingBindings();
    h.itemView.setOnClickListener(v -> {
      // TODO: navigate theo c.id nếu bạn muốn
    });
  }
  
  @Override public int getItemCount() { return items.size(); }
  
  public void submit(List<HomeApi.Category> data) {
    items.clear();
    if (data != null) items.addAll(data);
    notifyDataSetChanged();
  }
}
