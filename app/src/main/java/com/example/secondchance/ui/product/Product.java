package com.example.secondchance.ui.product;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;
    private List<String> imageUrls;
    private String postedDate;
    private String deletedDate;
    private String status;
    private String type;
    private String originalStatus;
    private long endTime;

    // Tab content
    private String description;
    private String source;
    private String proof;
    private String otherInfo;

    public Product() {
        imageUrls = new ArrayList<>();
    }

    public Product(String id, String name, double price, int quantity,
                   List<String> imageUrls, String postedDate, String status, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrls = imageUrls;
        this.postedDate = postedDate;
        this.status = status;
        this.type = type;
        this.originalStatus = status; // Mặc định giống status
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getPostedDate() { return postedDate; }
    public String getDeletedDate() { return deletedDate; }
    public String getStatus() { return status; }
    public String getType() { return type; }
    public String getOriginalStatus() { return originalStatus; }
    public long getEndTime() { return endTime; }
    public String getDescription() { return description; }
    public String getSource() { return source; }
    public String getProof() { return proof; }
    public String getOtherInfo() { return otherInfo; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public void setPostedDate(String postedDate) { this.postedDate = postedDate; }
    public void setDeletedDate(String deletedDate) { this.deletedDate = deletedDate; }
    public void setStatus(String status) { this.status = status; }
    public void setType(String type) { this.type = type; }
    public void setOriginalStatus(String originalStatus) { this.originalStatus = originalStatus; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public void setDescription(String description) { this.description = description; }
    public void setSource(String source) { this.source = source; }
    public void setProof(String proof) { this.proof = proof; }
    public void setOtherInfo(String otherInfo) { this.otherInfo = otherInfo; }

    // Helper methods
    public boolean isDeleted() {
        return "deleted".equals(status);
    }

    public String getDisplayDate() {
        return isDeleted() && deletedDate != null ? deletedDate : postedDate;
    }

    public String getDisplayDateLabel() {
        return isDeleted() ? "Ngày xóa: " : "Posted date: ";
    }
}
