package com.example.secondchance.data.model;

import com.google.gson.annotations.SerializedName;

public class ShopProfileResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private ShopData data;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public ShopData getData() {
        return data;
    }
}
