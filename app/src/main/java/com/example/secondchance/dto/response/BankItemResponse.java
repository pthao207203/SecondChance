package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

public class BankItemResponse {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public BankListResponse.BankItem data;
}
