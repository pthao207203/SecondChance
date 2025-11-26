package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;

public class ProfileUpdateRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

    @SerializedName("mail")
    private String mail;

    public ProfileUpdateRequest(String name, String phone, String mail) {
        this.name = name;
        this.phone = phone;
        this.mail = mail;
    }
}
