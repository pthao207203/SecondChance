package com.example.secondchance.ui.card;

import androidx.annotation.DrawableRes;
import java.util.Date;

/**
 * Lớp đại diện cho một sản phẩm trong danh sách card.
 * Chứa thông tin như hình ảnh, tiêu đề, mô tả, số lượng, đánh giá, giá, loại sản phẩm, thời gian đăng,
 * và chiều cao (có thể được sử dụng để điều chỉnh giao diện).
 */
public class ProductCard implements java.io.Serializable{
    private int id;
    private @DrawableRes int imageRes;
    private String title;
    private String description;
    private int quantity;
    private float starRating;
    private String price;
    private ProductType productType;
    private String timeRemaining; // Chỉ dùng cho Auction, định dạng "HH:MM:SS"
    private Date postTime; // Thời gian đăng bài
    private int height; // Chiều cao của card (dp), có thể được sử dụng để điều chỉnh giao diện

    /**
     * Enum định nghĩa các loại sản phẩm.
     */
    public enum ProductType {
        FIXED, AUCTION, NEGOTIATION
    }

    /**
     * Constructor khởi tạo ProductCard với các tham số cần thiết.
     *
     * @param id ID duy nhất của sản phẩm
     * @param imageRes Tài nguyên hình ảnh của sản phẩm
     * @param title Tiêu đề sản phẩm
     * @param description Mô tả sản phẩm
     * @param quantity Số lượng sản phẩm
     * @param starRating Đánh giá sao của sản phẩm
     * @param price Giá của sản phẩm
     * @param productType Loại sản phẩm (FIXED, AUCTION, NEGOTIATION)
     * @param postTime Thời gian đăng bài
     * @param height Chiều cao của card (dp)
     */
    public ProductCard(int id, @DrawableRes int imageRes, String title, String description,
                       int quantity, float starRating, String price, ProductType productType, Date postTime, int height) {
        this.id = id;
        this.imageRes = imageRes;
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.quantity = quantity;
        this.starRating = starRating;
        this.price = price != null ? price : "";
        this.productType = productType;
        this.timeRemaining = (productType == ProductType.AUCTION) ? "00:00:00" : null;
        this.postTime = postTime != null ? postTime : new Date(); // Mặc định là thời gian hiện tại nếu null
        this.height = height;
    }
    public ProductCard() {
        // Khởi tạo các giá trị mặc định để tránh lỗi
        this.title = "";
        this.description = "";
        this.price = "";
        this.postTime = new Date();
        this.productType = ProductType.FIXED; // Hoặc một giá trị mặc định
    }

    // Getters
    public int getId() { return id; }
    public @DrawableRes int getImageRes() { return imageRes; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getQuantity() { return quantity; }
    public float getStarRating() { return starRating; }
    public String getPrice() { return price; }
    public ProductType getProductType() { return productType; }
    public String getTimeRemaining() { return timeRemaining != null ? timeRemaining : ""; }
    public Date getPostTime() { return postTime; }
    public int getHeight() { return height; }

    // Setters
    public void setTimeRemaining(String timeRemaining) {
        if (productType == ProductType.AUCTION && timeRemaining != null) {
            this.timeRemaining = timeRemaining;
        }
    }
    public void setTitle(String title) { this.title = title != null ? title : ""; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }
    public void setPrice(String price) { this.price = price != null ? price : ""; }
    public void setPostTime(Date postTime) { this.postTime = postTime != null ? postTime : new Date(); }
    public void setHeight(int height) { this.height = height; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
    }
    public void setImageRes(@DrawableRes int imageRes) {
        this.imageRes = imageRes;
    }

}
