package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;

public class PasswordUpdateRequest {
    @SerializedName("oldPassword")
    private String oldPassword;

    @SerializedName("newPassword")
    private String newPassword;

    public PasswordUpdateRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
