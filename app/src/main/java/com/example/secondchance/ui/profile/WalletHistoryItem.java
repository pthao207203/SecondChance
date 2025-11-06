package com.example.secondchance.ui.profile;

public class WalletHistoryItem {
  public final String thumbRes;
  public final String title;
  public final String sub;
  public final String price;
  public String orderId;
  
  public WalletHistoryItem(String thumbRes, String title, String sub, String price, String orderId) {
    this.thumbRes = thumbRes;
    this.title = title;
    this.sub = sub;
    this.price = price;
    this.orderId = orderId;
  }
}