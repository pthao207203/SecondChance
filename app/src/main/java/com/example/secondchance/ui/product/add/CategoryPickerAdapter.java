package com.example.secondchance.ui.product.add;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.secondchance.R;
import com.example.secondchance.dto.response.ProductMetaResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryPickerAdapter
  extends RecyclerView.Adapter<CategoryPickerAdapter.CategoryVH> {
  
  private final List<ProductMetaResponse.Category> categories;
  private final Set<String> selectedIds = new HashSet<>();
  
  public CategoryPickerAdapter(List<ProductMetaResponse.Category> categories,
                               List<String> preSelected) {
    this.categories = categories != null ? categories : new ArrayList<>();
    if (preSelected != null) {
      selectedIds.addAll(preSelected);
    }
  }
  
  @NonNull
  @Override
  public CategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
      .inflate(R.layout.item_category_checkbox, parent, false);
    return new CategoryVH(v);
  }
  
  @Override
  public void onBindViewHolder(@NonNull CategoryVH holder, int position) {
    ProductMetaResponse.Category c = categories.get(position);
    holder.tvName.setText(c.name);
    
    boolean checked = selectedIds.contains(c.id);
    holder.ivCheck.setImageResource(
      checked ? R.drawable.ic_checkbox_checked
        : R.drawable.ic_checkbox_unchecked
    );
    
    holder.itemView.setOnClickListener(v -> {
      toggle(c.id);
      notifyItemChanged(holder.getBindingAdapterPosition());
    });
  }
  
  @Override
  public int getItemCount() {
    return categories.size();
  }
  
  private void toggle(String id) {
    if (selectedIds.contains(id)) {
      selectedIds.remove(id);
    } else {
      selectedIds.add(id);
    }
  }
  
  public ArrayList<String> getSelectedIds() {
    return new ArrayList<>(selectedIds);
  }
  
  static class CategoryVH extends RecyclerView.ViewHolder {
    ImageView ivCheck;
    TextView tvName;
    
    CategoryVH(@NonNull View itemView) {
      super(itemView);
      ivCheck = itemView.findViewById(R.id.ivCheck);
      tvName = itemView.findViewById(R.id.tvName);
    }
  }
}
