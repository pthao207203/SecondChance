package com.example.secondchance.ui.product.search;

import android.os.Bundle;

public class ProductFilterArgs {
  public String pickupCity;
  public Integer minRate;
  public Integer status;
  public Integer minPrice;
  public Integer maxPrice;
  public Integer priceType;
  public String categoryId;
  
  public int page = 1;
  public int pageSize = 20;
  
  public static final String KEY_PICKUP_CITY = "pickupCity";
  public static final String KEY_MIN_RATE    = "minRate";
  public static final String KEY_STATUS      = "status";
  public static final String KEY_MIN_PRICE   = "minPrice";
  public static final String KEY_MAX_PRICE   = "maxPrice";
  public static final String KEY_PRICE_TYPE  = "priceType";
  public static final String KEY_CATEGORY_ID = "categoryId";
  
  public static ProductFilterArgs fromBundle(Bundle args) {
    ProductFilterArgs out = new ProductFilterArgs();
    if (args == null) return out;
    
    out.pickupCity = args.getString(KEY_PICKUP_CITY);
    if (args.containsKey(KEY_MIN_RATE))   out.minRate   = args.getInt(KEY_MIN_RATE);
    if (args.containsKey(KEY_STATUS))     out.status    = args.getInt(KEY_STATUS);
    if (args.containsKey(KEY_MIN_PRICE))  out.minPrice  = args.getInt(KEY_MIN_PRICE);
    if (args.containsKey(KEY_MAX_PRICE))  out.maxPrice  = args.getInt(KEY_MAX_PRICE);
    if (args.containsKey(KEY_PRICE_TYPE)) out.priceType = args.getInt(KEY_PRICE_TYPE);
    out.categoryId = args.getString(KEY_CATEGORY_ID);
    
    return out;
  }
  
  public void putToBundle(Bundle out) {
    if (pickupCity != null) out.putString(KEY_PICKUP_CITY, pickupCity);
    if (minRate   != null) out.putInt(KEY_MIN_RATE, minRate);
    if (status    != null) out.putInt(KEY_STATUS, status);
    if (minPrice  != null) out.putInt(KEY_MIN_PRICE, minPrice);
    if (maxPrice  != null) out.putInt(KEY_MAX_PRICE, maxPrice);
    if (priceType != null) out.putInt(KEY_PRICE_TYPE, priceType);
    if (categoryId != null) out.putString(KEY_CATEGORY_ID, categoryId);
  }
}
