package com.example.secondchance.ui.message;

public class MessageDetail {
    private String content;
    private String time;        // HH:mm
    private long timestamp;     // THÊM DÒNG NÀY (milliseconds)
    private boolean isMine;
    private String avatarUrl;

    public MessageDetail(String content, String time, long timestamp, boolean isMine, String avatarUrl) {
        this.content = content;
        this.time = time;
        this.timestamp = timestamp;
        this.isMine = isMine;
        this.avatarUrl = avatarUrl;
    }

    // Getters
    public String getContent() { return content; }
    public String getTime() { return time; }
    public long getTimestamp() { return timestamp; }
    public boolean isMine() { return isMine; }
    public String getAvatarUrl() { return avatarUrl; }
}