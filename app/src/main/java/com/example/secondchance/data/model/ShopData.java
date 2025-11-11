package com.example.secondchance.data.model;

import com.google.gson.annotations.SerializedName;

import com.example.secondchance.data.model.AddressData;

public class ShopData {

    @SerializedName("name")
    private String name;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("mail")
    private String mail;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private AddressData address;

    @SerializedName("status")
    private String status;

    @SerializedName("walletBalance")
    private double walletBalance;

    // Getters
    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

}