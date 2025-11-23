package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PreviewOrderRequest {

    @SerializedName("items")
    private List<Item> items;

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        @SerializedName("productId")
        private String productId;

        @SerializedName("qty")
        private int qty;

        public Item(String productId, int qty) {
            this.productId = productId;
            this.qty = qty;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }
    }
}