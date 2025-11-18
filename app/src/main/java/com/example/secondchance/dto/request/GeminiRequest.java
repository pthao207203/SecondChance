package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GeminiRequest {
  private List<Content> contents;
  
  public GeminiRequest(List<Content> contents) {
    this.contents = contents;
  }
  
  public static class Content {
    private String role; // "user" hoặc "model"
    private List<Part> parts;
    
    public Content(String role, List<Part> parts) {
      this.role = role;
      this.parts = parts;
    }
  }
  
  public static class Part {
    private String text;
    
    @SerializedName("inlineData")
    public InlineData inlineData;
    
    public Part(String text) {
      this.text = text;
    }
  }
  public static class InlineData {
    @SerializedName("mimeType")
    public String mimeType;
    @SerializedName("data")
    public String data; // Chuỗi Base64
    
    public InlineData(String mimeType, String data) {
      this.mimeType = mimeType;
      this.data = data;
    }
  }
}
