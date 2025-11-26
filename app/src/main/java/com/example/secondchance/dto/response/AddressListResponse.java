package com.example.secondchance.dto.response;

import com.example.secondchance.data.model.AddressData;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AddressListResponse {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("items")
        public List<AddressData> items;
    }
}
