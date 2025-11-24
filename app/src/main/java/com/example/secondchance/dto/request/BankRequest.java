package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;

public class BankRequest {
    @SerializedName("bankName")
    private String bankName;
    @SerializedName("accountNumber")
    private String accountNumber;
    @SerializedName("accountHolder")
    private String accountHolder;
    @SerializedName("isDefault")
    private String isDefault;

    public BankRequest(String bankName, String accountNumber, String accountHolder) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
    }
    
    public BankRequest(String bankName, String accountNumber, String accountHolder, String isDefault) {
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.isDefault = isDefault;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public void setAccountHolder(String accountHolder) {
        this.accountHolder = accountHolder;
    }
    
    public String getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }
}
