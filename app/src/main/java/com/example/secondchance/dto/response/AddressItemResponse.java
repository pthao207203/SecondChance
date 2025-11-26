package com.example.secondchance.dto.response;

import com.example.secondchance.data.model.AddressData;
import com.google.gson.annotations.SerializedName;

public class AddressItemResponse {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public AddressData data;
}
