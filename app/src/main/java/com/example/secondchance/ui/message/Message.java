package com.example.secondchance.ui.message;

import java.util.Objects;

public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String lastMessage;
    private long timestamp;
    private String avatarUrl;
    private boolean isShop;      // true = shop, false = khách
    private boolean isUnread;    // có tin chưa đọc không

    // Constructor rỗng (cần cho Firebase nếu dùng)
    public Message() {}

    // Constructor đầy đủ
    public Message(String id, String senderId, String senderName, String lastMessage,
                   long timestamp, String avatarUrl, boolean isShop, boolean isUnread) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.avatarUrl = avatarUrl;
        this.isShop = isShop;
        this.isUnread = isUnread;
    }

    // === GETTER & SETTER ===
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isShop() { return isShop; }
    public void setShop(boolean shop) { isShop = shop; }

    public boolean isUnread() { return isUnread; }
    public void setUnread(boolean unread) { isUnread = unread; }

    // Optional: để dùng trong List.contains(), equals, hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id != null && id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}