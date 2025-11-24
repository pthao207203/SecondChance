package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;

public class BankListRequest {
    @SerializedName("userPhone")
    private String userPhone;
    @SerializedName("password")
    private String password;

    public BankListRequest(String userPhone, String password) {
        this.userPhone = userPhone;
        this.password = password;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
