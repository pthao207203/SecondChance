package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductCreateRequest {
  
  @SerializedName("productName")
  public String productName;
  
  @SerializedName("productDescription")
  public String productDescription;
  
  @SerializedName("productUsageTime")
  public int productUsageTime;
  
  @SerializedName("productMedia")
  public List<String> productMedia;
  
  @SerializedName("productPrice")
  public Long productPrice;
  
  @SerializedName("productPriceType")
  public int productPriceType;
  
  @SerializedName("productAution")
  public ProductAuction productAution;
  
  @SerializedName("productQuantity")
  public int productQuantity;
  
  @SerializedName("productCategory")
  public List<String> productCategory;
  
  @SerializedName("productBrand")
  public String productBrand;
  
  public Boolean  productConditionNote;   // "Chưa sử dụng"
  public Integer productNewPercent;      // 99
  public Integer productDamagePercent;   // 0
  public Integer productWarrantyMonths;  // 3
  public Boolean productReturnPolicy;    // true / false
  public Boolean productHasOrigin;       // true / false
  public OriginLink productOriginLink;   // object con
  
  public static class OriginLink {
    public String description; // mô tả nguồn
    public String url;         // link
  }
  
  public static class ProductAuction {
    @SerializedName("startingPrice")
    public long startingPrice;
    
    @SerializedName("startsAt")
    public String startsAt;
    
    @SerializedName("endsAt")
    public String endsAt;
    
    @SerializedName("condition")
    public String condition;
    
    @SerializedName("featured")
    public boolean featured;
  }
}
