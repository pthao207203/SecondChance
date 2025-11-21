package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaymentRequest {

    @SerializedName("items")
    private List<Item> items;

    @SerializedName("shippingAddress")
    private ShippingAddress shippingAddress;

    @SerializedName("shippingFee")
    private long shippingFee;

    // --- Getters & Setters ---

    public long getShippingFee() { return shippingFee; }
    public void setShippingFee(long shippingFee) { this.shippingFee = shippingFee; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public ShippingAddress getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(ShippingAddress shippingAddress) { this.shippingAddress = shippingAddress; }

    // --- Inner Classes ---

    public static class Item {
        @SerializedName("productId")
        private String productId;

        @SerializedName("qty")
        private int qty;

        public Item(String productId, int qty) {
            this.productId = productId;
            this.qty = qty;
        }

        // Getters & Setters
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public int getQty() { return qty; }
        public void setQty(int qty) { this.qty = qty; }
    }

    public static class ShippingAddress {
        @SerializedName("fullName")
        private String fullName;

        @SerializedName("phone")
        private String phone;

        @SerializedName("address")
        private String address;

        public ShippingAddress() { }

        public ShippingAddress(String fullName, String phone, String address) {
            this.fullName = fullName;
            this.phone = phone;
            this.address = address;
        }

        // Getters & Setters
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }
}
