package com.example.secondchance.ui.chatbot;

public class Message {
  public boolean isUser;
  public String text;
  
  public Message(boolean isUser, String text) {
    this.isUser = isUser;
    this.text = text;
  }
}

