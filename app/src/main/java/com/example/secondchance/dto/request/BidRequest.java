package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;

public class BidRequest {
    @SerializedName("amount")
    public long amount;

    public BidRequest(long amount) {
        this.amount = amount;
    }
}
