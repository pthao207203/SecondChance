package com.example.secondchance.ui.profile;

public class WalletHistoryItem {
  public final int thumbRes;
  public final String title;
  public final String sub;
  public final String price;
  
  public WalletHistoryItem(int thumbRes, String title, String sub, String price) {
    this.thumbRes = thumbRes;
    this.title = title;
    this.sub = sub;
    this.price = price;
  }
}