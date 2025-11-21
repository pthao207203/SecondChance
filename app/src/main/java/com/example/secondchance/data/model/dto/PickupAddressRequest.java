package com.example.secondchance.data.model.dto;

import com.google.gson.annotations.SerializedName;

public class PickupAddressRequest {

    @SerializedName("address")
    private String address;

    @SerializedName("city")
    private String city; //

    @SerializedName("province")
    private String province;

    @SerializedName("postalCode")
    private String postalCode;

    @SerializedName("contactName")
    private String contactName;

    @SerializedName("contactPhone")
    private String contactPhone;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    // Constructor
    public PickupAddressRequest(String address, String city, String province, String postalCode, String contactName, String contactPhone) {
        this.address = address;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.lat = 0.0;
        this.lng = 0.0;
    }
}
