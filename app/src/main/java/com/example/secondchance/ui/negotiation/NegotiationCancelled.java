package com.example.secondchance.ui.negotiation;

public class NegotiationCancelled {
    private String productName;
    private String productDate;
    private String negotiationRound;
    private String title;
    private String price;
    private String quantity;
    private String fixedPriceText;
    private String createdDate;
    private String shopName;
    private String shopDate;
    private String replyMessage;
    private boolean isOverdue;  // true = quá hạn, false = bị từ chối bình thường

    public NegotiationCancelled(String productName, String productDate, String negotiationRound,
                                String title, String price, String quantity, String fixedPriceText,
                                String createdDate, String shopName, String shopDate,
                                String replyMessage, boolean isOverdue) {
        this.productName = productName;
        this.productDate = productDate;
        this.negotiationRound = negotiationRound;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.fixedPriceText = fixedPriceText;
        this.createdDate = createdDate;
        this.shopName = shopName;
        this.shopDate = shopDate;
        this.replyMessage = replyMessage;
        this.isOverdue = isOverdue;
    }

    public String getProductName() { return productName; }
    public String getProductDate() { return productDate; }
    public String getNegotiationRound() { return negotiationRound; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getFixedPriceText() { return fixedPriceText; }
    public String getCreatedDate() { return createdDate; }
    public String getShopName() { return shopName; }
    public String getShopDate() { return shopDate; }
    public String getReplyMessage() { return replyMessage; }
    public boolean isOverdue() { return isOverdue; }
}
