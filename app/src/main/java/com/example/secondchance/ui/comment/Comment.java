// ui/comment/Comment.java
package com.example.secondchance.ui.comment;

public class Comment {
    private String name;
    private String date;
    private String product;
    private String content;
    private String rating;
    private String reply; // null nếu không có

    public Comment(String name, String date, String product, String content, String rating, String reply) {
        this.name = name;
        this.date = date;
        this.product = product;
        this.content = content;
        this.rating = rating;
        this.reply = reply;
    }

    // Getters
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getProduct() { return product; }
    public String getContent() { return content; }
    public String getRating() { return rating; }
    public String getReply() { return reply; }

    public boolean hasReply() {
        return reply != null && !reply.isEmpty();
    }
}
