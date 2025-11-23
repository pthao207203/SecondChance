package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {

    @SerializedName("appTransId")
    private String appTransId;

    @SerializedName("payUrl")
    private String payUrl;

    // --- Getters ---
    public String getPayUrl() {
        return payUrl;
    }

    public String getAppTransId() {
        return appTransId;
    }
}