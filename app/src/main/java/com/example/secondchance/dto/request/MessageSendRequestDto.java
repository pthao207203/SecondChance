package com.example.secondchance.dto.request;

public class MessageSendRequestDto {
  private String receiverId;
  private String contentType; // "text" hoặc "image"
  private String content;     // text hoặc URL ảnh
  
  public MessageSendRequestDto(String receiverId, String contentType, String content) {
    this.receiverId = receiverId;
    this.contentType = contentType;
    this.content = content;
  }
  
  public String getReceiverId() {
    return receiverId;
  }
  
  public String getContentType() {
    return contentType;
  }
  
  public String getContent() {
    return content;
  }
}
