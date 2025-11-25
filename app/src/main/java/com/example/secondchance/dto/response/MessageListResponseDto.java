package com.example.secondchance.dto.response;

import java.util.List;

public class MessageListResponseDto {
  private boolean success;
  private MessageDataDto data;
  
  public boolean isSuccess() {
    return success;
  }
  
  public MessageDataDto getData() {
    return data;
  }
  
  public static class MessageDataDto {
    private java.util.List<MessageItemDto> items;
    private int page;
    private int pageSize;
    private int total;
    
    public List<MessageItemDto> getItems() {
      return items;
    }
    
    public int getPage() {
      return page;
    }
    
    public int getPageSize() {
      return pageSize;
    }
    
    public int getTotal() {
      return total;
    }
  }
  public static class MessageItemDto {
    
    private String id;
    private String senderId;
    private String contentType;
    private String content;
    private List<MessageAttachmentDto> attachments;
    private String status;
    private String sentAt;
    private String readAt;  // có thể null
    
    public String getId() {
      return id;
    }
    
    public String getSenderId() {
      return senderId;
    }
    
    public String getContentType() {
      return contentType;
    }
    
    public String getContent() {
      return content;
    }
    
    public List<MessageAttachmentDto> getAttachments() {
      return attachments;
    }
    
    public String getStatus() {
      return status;
    }
    
    public String getSentAt() {
      return sentAt;
    }
    
    public void setId(String id) {
      this.id = id;
    }
    
    public void setSenderId(String senderId) {
      this.senderId = senderId;
    }
    
    public void setContentType(String contentType) {
      this.contentType = contentType;
    }
    
    public void setContent(String content) {
      this.content = content;
    }
    
    public void setAttachments(List<MessageAttachmentDto> attachments) {
      this.attachments = attachments;
    }
    
    public void setStatus(String status) {
      this.status = status;
    }
    
    public void setSentAt(String sentAt) {
      this.sentAt = sentAt;
    }
    
    public void setReadAt(String readAt) {
      this.readAt = readAt;
    }
    
    public String getReadAt() {
      return readAt;
    }
  }
  public class MessageAttachmentDto {
    private String type;
    private String url;
    
    public String getType() {
      return type;
    }
    
    public String getUrl() {
      return url;
    }
  }
}
