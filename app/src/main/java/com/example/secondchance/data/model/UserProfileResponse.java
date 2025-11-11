package com.example.secondchance.data.model;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private UserData data;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public UserData getData() {
        return data;
    }
}