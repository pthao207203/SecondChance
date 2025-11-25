package com.example.secondchance.dto.response;
import java.util.List;

public class ConversationListResponseDto {
  private boolean success;
  private ConversationDataDto data;
  
  public boolean isSuccess() {
    return success;
  }
  
  public ConversationDataDto getData() {
    return data;
  }
  
  public static class ConversationDataDto {
    private List<ConversationItemDto> items;
    
    public List<ConversationItemDto> getItems() {
      return items;
    }
  }
  public class ConversationItemDto {
    
    private ConversationUserDto user;
    private String lastContent;
    private String lastContentType;
    private String lastSentAt;   // ISO string: "2025-11-24T09:29:48.853Z"
    private int unreadCount;
    
    public ConversationUserDto getUser() {
      return user;
    }
    
    public String getLastContent() {
      return lastContent;
    }
    
    public String getLastContentType() {
      return lastContentType;
    }
    
    public String getLastSentAt() {
      return lastSentAt;
    }
    
    public int getUnreadCount() {
      return unreadCount;
    }
  }
  public class ConversationUserDto {
    private String id;
    private String userName;
    private String userAvatar;
    
    public String getId() {
      return id;
    }
    
    public String getUserName() {
      return userName;
    }
    
    public String getUserAvatar() {
      return userAvatar;
    }
  }
}
