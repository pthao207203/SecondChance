package com.example.secondchance.ui.chatbot;

import android.graphics.Bitmap;

public class MessageModel {
  private String message;
  private boolean isUser; // true: người dùng, false: bot
  private Bitmap image;   // Ảnh (nếu có)
  
  public MessageModel(String message, boolean isUser) {
    this.message = message;
    this.isUser = isUser;
  }
  
  // Constructor có ảnh
  public MessageModel(String message, boolean isUser, Bitmap image) {
    this.message = message;
    this.isUser = isUser;
    this.image = image;
  }
  
  public String getMessage() { return message; }
  public boolean isUser() { return isUser; }
  public Bitmap getImage() { return image; }
  public void setImage(Bitmap image) { this.image = image; }
}
