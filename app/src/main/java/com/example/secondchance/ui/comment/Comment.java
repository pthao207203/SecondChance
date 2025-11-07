package com.example.secondchance.ui.comment;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public class Comment {
    @SerializedName("_id")
    private String id;

    @SerializedName("by")
    private User by;  // Nested object chứa thông tin user

    @SerializedName("rate")
    private int rating;

    @SerializedName("description")
    private String content;

    @SerializedName("media")
    private List<String> media;

    @SerializedName("createdAt")
    private String createdAt;

    // --- Nested class để ánh xạ user ---
    public static class User {
        @SerializedName("_id")
        private String userId;

        @SerializedName("name")
        private String name;

        @SerializedName("avatar")
        private String avatarUrl;

        public String getUserId() { return userId; }
        public String getName() { return name != null ? name : "Khách hàng"; }
        public String getAvatarUrl() { return avatarUrl; }
    }

    // --- Getter chính ---
    public String getId() { return id; }
    public String getName() { return by != null ? by.getName() : "Khách hàng"; }
    public String getAvatarUrl() { return by != null ? by.getAvatarUrl() : null; }
    public String getDate() { return createdAt; }
    public String getContent() { return content != null ? content : ""; }
    public int getRating() { return rating; }
    public List<String> getMedia() { return media != null ? media : Collections.emptyList(); }
    public boolean hasMedia() { return media != null && !media.isEmpty(); }
}
