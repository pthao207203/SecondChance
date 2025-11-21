package com.example.secondchance.data.model.dto;

import com.google.gson.annotations.SerializedName;

public class BecomeSellerRequest {

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("shopName")
    private String shopName;

    @SerializedName("idNumber")
    private String idNumber;

    @SerializedName("idFrontUrl")
    private String idFrontUrl;

    @SerializedName("idBackUrl")
    private String idBackUrl;

    @SerializedName("pickupAddress")
    private PickupAddressRequest pickupAddress;

    // Constructor
    public BecomeSellerRequest(String fullName, String shopName, String idNumber, String idFrontUrl, String idBackUrl, PickupAddressRequest pickupAddress) {
        this.fullName = fullName;
        this.shopName = shopName;
        this.idNumber = idNumber;
        this.idFrontUrl = idFrontUrl;
        this.idBackUrl = idBackUrl;
        this.pickupAddress = pickupAddress;
    }
}
