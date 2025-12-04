package com.example.secondchance.data.model;

import com.google.gson.annotations.SerializedName;

public class UserData {

    @SerializedName("_id")
    private String id;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("name")
    private String name;
    
    @SerializedName("shopName")
    private String shopName;
    @SerializedName("mail")
    private String mail;

    @SerializedName("phone")
    private String phone;

    @SerializedName("walletBalance")
    private long walletBalance;

    @SerializedName("address")
    private AddressData address;

    // Getters
    public String getId() { return id; }
    public String getAvatar() { return avatar; }
    public long getWalletBalance() { return walletBalance; }

    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }

    public AddressData getAddress() {
        return address;
    }
}
