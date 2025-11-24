package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BankListResponse {
    @SerializedName("success")
    public boolean success;
    @SerializedName("data")
    public Data data;

    public static class Data {
        @SerializedName("items")
        public List<BankItem> items;
    }

    public static class BankItem {
        @SerializedName("bankName")
        public String bankName;
        @SerializedName("accountNumber")
        public String accountNumber;
        @SerializedName("accountHolder")
        public String accountHolder;
        @SerializedName("isDefault")
        public boolean isDefault;
    }
}
