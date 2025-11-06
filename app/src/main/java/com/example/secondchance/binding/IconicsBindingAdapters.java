package com.example.secondchance.binding;

import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.mikepenz.iconics.IconicsDrawable;

public class IconicsBindingAdapters {
  
  // Bạn có thể truyền icon theo tên (gmd_home, cmdi_sofa, ...)
  // và chọn 1 trong 2 cách tô màu: colorInt hoặc colorRes.
  @BindingAdapter("iiv_icon")
  public static void setIivIcon(com.mikepenz.iconics.view.IconicsImageView v, String name) {
    if (name == null || name.isEmpty()) {
      v.setIcon((com.mikepenz.iconics.IconicsDrawable) null);
      return;
    }
    
    // chuẩn hóa tên icon (vì backend có thể có '-')
    name = name.replace('-', '_');
    
    // tạo IconicsDrawable thủ công và gán vào view
    com.mikepenz.iconics.IconicsDrawable drawable =
      new com.mikepenz.iconics.IconicsDrawable(v.getContext(), name);
    
    v.setIcon(drawable);
  }
}

