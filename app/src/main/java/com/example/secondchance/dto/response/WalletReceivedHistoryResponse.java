package com.example.secondchance.dto.response;

import java.util.List;

public class WalletReceivedHistoryResponse {
  public boolean success;
  public Data data;
  
  public static class Data {
    public int page;
    public int pageSize;
    public int total;
    public List<Item> items;
  }
  
  public static class Item {
    public FirstProduct firstProduct;
    public String orderId;
    public String type;     // "refund" | "topup" | "transfer" ... (tuỳ backend)
    public String ref;      // ví dụ "REFUND-68ff0d912929e7dbca98ae22-1761589356046"
    public long amount;     // 7000000
    public String currency; // "VND"
    public String time;     // ISO8601 "2025-10-27T18:22:36.046Z"
  }
  public static class FirstProduct {
    public String id;
    public String image;
    public String name;
  }
}
