package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PreviewOrderResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("data")
    public Data data;

    public Data getData() { return data; }

    public static class Data {
        @SerializedName("totalPrice")
        public long totalPrice;

        @SerializedName("shippingFee")
        public long shippingFee;

        @SerializedName("grandTotal")
        public long grandTotal;

        @SerializedName("items")
        public List<ShopGroup> items;

        public long getGrandTotal() { return grandTotal; }
        public long getShippingFee() { return shippingFee; }
        public List<ShopGroup> getItems() { return items; }
    }

    public static class ShopGroup {
        @SerializedName("shopId")
        public String shopId;

        @SerializedName("shopName")
        public String shopName;

        @SerializedName("items")
        public List<PreviewItem> items;
    }

    public static class PreviewItem {
        @SerializedName("productId")
        public String productId;

        @SerializedName("name")
        public String name;

        @SerializedName("imageUrl")
        public String imageUrl;

        @SerializedName("price")
        public long price;

        @SerializedName("qty")
        public int qty;

        @SerializedName("lineTotal")
        public long lineTotal;
    }
}
