package com.example.secondchance.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class OrderWrapper implements Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("order")
    public Order order;
    
    @SerializedName("shipment")
    public ShipmentData shipment;
    
    public static class ShipmentData implements Serializable {
        @SerializedName("currentStatus")
        public int currentStatus;
        
        @SerializedName("trackingNumber")
        public String trackingNumber;
        
    }
}