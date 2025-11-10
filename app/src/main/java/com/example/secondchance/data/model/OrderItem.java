package com.example.secondchance.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class OrderItem implements Serializable {

    @SerializedName("_id")
    public String id;

    @SerializedName("productId")
    public String productId;

    @SerializedName("name")
    public String name;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("price")
    public long price;

    @SerializedName("qty")
    public int quantity;
    
    public OrderItem(String productId, String name, String imageUrl, long price, int quantity) {
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }
    public OrderItem() {}
    
    public String getId() { return id; }
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public long getPrice() { return price; }
    public int getQuantity() { return quantity; }
}