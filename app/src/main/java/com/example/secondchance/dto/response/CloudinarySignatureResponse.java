package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

public class CloudinarySignatureResponse {
  
  @SerializedName("cloudName")
  public String cloudName;
  
  @SerializedName("apiKey")
  public String apiKey;
  
  @SerializedName("timestamp")
  public long timestamp;
  
  @SerializedName("signature")
  public String signature;
  
  @SerializedName("folder")
  public String folder; // có thể null
}
