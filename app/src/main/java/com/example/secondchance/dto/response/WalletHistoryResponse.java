package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WalletHistoryResponse {
  @SerializedName("success") public boolean success;
  @SerializedName("data")    public Data data;
  
  public static class Data {
    @SerializedName("items") public List<Item> items;
  }
  
  public static class Item {
    // Backend trả VND theo đơn vị đồng (âm là chi, dương là nạp)
    @SerializedName("amount")   public long amount;
    @SerializedName("currency") public String currency; // "VND"
    @SerializedName("time")     public String time;     // ISO 8601, ví dụ "2025-10-24T05:12:40.555Z"
  }
}
