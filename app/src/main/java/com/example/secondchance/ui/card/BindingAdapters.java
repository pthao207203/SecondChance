package com.example.secondchance.ui.card;

import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;

public class BindingAdapters {
  
  @BindingAdapter(value = {"imageUrl", "placeholder"}, requireAll = false)
  public static void setImageUrl(ImageView iv, String url, @DrawableRes int placeholder) {
    if (!TextUtils.isEmpty(url)) {
      Glide.with(iv.getContext())
        .load(url)
        .placeholder(placeholder != 0 ? placeholder : 0)
        .into(iv);
    } else if (placeholder != 0) {
      iv.setImageResource(placeholder);
    }
  }
}
