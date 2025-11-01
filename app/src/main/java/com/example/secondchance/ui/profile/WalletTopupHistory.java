package com.example.secondchance.ui.profile;

public class WalletTopupHistory {
  public final String title;
  public final String date;     // dd/MM/yyyy
  public final long amount;     // số tiền (VND)
  
  public WalletTopupHistory(String title, String date, long amount) {
    this.title = title;
    this.date = date;
    this.amount = amount;
  }
}
