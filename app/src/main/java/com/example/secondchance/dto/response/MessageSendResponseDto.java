package com.example.secondchance.dto.response;

import java.util.List;

public class MessageSendResponseDto {
  private boolean success;
  private MessageData data;
  
  public boolean isSuccess() {
    return success;
  }
  
  public MessageData getData() {
    return data;
  }
  
  public static class MessageData {
    private String id;
    private String conversationId;
    private String senderId;
    private String contentType;
    private String content;
    private java.util.List<MessageListResponseDto.MessageAttachmentDto> attachments;
    private String status;
    private String sentAt;
    private String readAt; // có thể null
    
    public String getId() { return id; }
    public String getConversationId() { return conversationId; }
    public String getSenderId() { return senderId; }
    public String getContentType() { return contentType; }
    public String getContent() { return content; }
    public List<MessageListResponseDto.MessageAttachmentDto> getAttachments() { return attachments; }
    public String getStatus() { return status; }
    public String getSentAt() { return sentAt; }
    public String getReadAt() { return readAt; }
  }
}
