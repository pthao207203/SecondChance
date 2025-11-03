package com.example.secondchance.ui.negotiation;

public class NegotiationCompleted {

    private String productName;
    private String productDate;
    private String negotiationRound;
    private String title;
    private String price;
    private String quantity;
    private String fixedPriceText;
    private String createdDate;
    private String replyMessage;

    public NegotiationCompleted(String productName, String productDate, String negotiationRound,
                                String title, String price, String quantity,
                                String fixedPriceText, String createdDate,
                                String replyMessage) {
        this.productName = productName;
        this.productDate = productDate;
        this.negotiationRound = negotiationRound;
        this.title = title;
        this.price = price;
        this.quantity = quantity;
        this.fixedPriceText = fixedPriceText;
        this.createdDate = createdDate;
        this.replyMessage = replyMessage;
    }

    public String getProductName() { return productName; }
    public String getProductDate() { return productDate; }
    public String getNegotiationRound() { return negotiationRound; }
    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getQuantity() { return quantity; }
    public String getFixedPriceText() { return fixedPriceText; }
    public String getCreatedDate() { return createdDate; }
    public String getReplyMessage() { return replyMessage; }
}

