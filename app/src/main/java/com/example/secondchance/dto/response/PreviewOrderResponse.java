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
        
        // 👉 thêm: danh sách địa chỉ
        @SerializedName("addresses")
        public List<Address> addresses;
        
        public long getGrandTotal() { return grandTotal; }
        public long getShippingFee() { return shippingFee; }
        public List<ShopGroup> getItems() { return items; }
        public List<Address> getAddresses() { return addresses; }
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
    
    // 👉 thêm class Address + Location
    public static class Address {
        @SerializedName("name")
        public String name;
        
        @SerializedName("phone")
        public String phone;
        
        @SerializedName("label")
        public String label;
        
        @SerializedName("country")
        public String country;
        
        @SerializedName("province")
        public String province;
        
        @SerializedName("ward")
        public String ward;
        
        @SerializedName("street")
        public String street;
        
        @SerializedName("isDefault")
        public boolean isDefault;
        
        @SerializedName("location")
        public Location location;
        
        @SerializedName("_id")
        public String id;
    }
    
    public static class Location {
        @SerializedName("lat")
        public double lat;
        
        @SerializedName("lng")
        public double lng;
    }
}
