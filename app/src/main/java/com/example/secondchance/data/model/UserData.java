package com.example.secondchance.data.model;

import com.google.gson.annotations.SerializedName;

public class UserData {

    @SerializedName("name")
    private String name;

    @SerializedName("mail")
    private String mail;

    @SerializedName("phone")
    private String phone;

//    @SerializedName("banks")
//    private BankData banks;

    @SerializedName("address")
    private AddressData address;

    // Getters
    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }

//    public BankData getBanks() {
//        return banks;
//    }

    public AddressData getAddress() {
        return address;
    }
}