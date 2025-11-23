package com.example.secondchance.ui.product.add;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.secondchance.R;
import com.example.secondchance.dto.response.ProductMetaResponse;

import java.util.List;

public class BrandSpinnerAdapter extends ArrayAdapter<ProductMetaResponse.Brand> {
  
  private final LayoutInflater inflater;
  
  public BrandSpinnerAdapter(@NonNull Context context,
                             @NonNull List<ProductMetaResponse.Brand> brands) {
    super(context, 0, brands);
    inflater = LayoutInflater.from(context);
  }
  
  @NonNull
  @Override
  public View getView(int position,
                      @Nullable View convertView,
                      @NonNull ViewGroup parent) {
    // View hiển thị khi spinner đóng
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.item_brand_spinner, parent, false);
    }
    bind(position, convertView);
    return convertView;
  }
  
  @Override
  public View getDropDownView(int position,
                              @Nullable View convertView,
                              @NonNull ViewGroup parent) {
    // View trong danh sách dropdown
    if (convertView == null) {
      convertView = inflater.inflate(R.layout.item_brand_dropdown, parent, false);
    }
    bind(position, convertView);
    return convertView;
  }
  
  private void bind(int position, View view) {
    ProductMetaResponse.Brand item = getItem(position);
    if (item == null) return;
    
    TextView tvName = view.findViewById(R.id.tvBrandName);
    ImageView imgLogo = view.findViewById(R.id.imgBrandLogo);
    
    tvName.setText(item.name != null ? item.name : "");
    
    // Logo từ URL
    Glide.with(view)
      .load(item.logo)
      .placeholder(R.drawable.ic_placeholder)
      .error(R.drawable.ic_placeholder)
      .fitCenter()
      .into(imgLogo);
  }
}

