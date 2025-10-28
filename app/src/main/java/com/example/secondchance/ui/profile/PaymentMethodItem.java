package com.example.secondchance.ui.profile;

import java.io.Serializable;

public class PaymentMethodItem implements Serializable {
    private String accountHolderName;
    private String bankName;
    private String accountNumber;
    private boolean isDefault;

    public PaymentMethodItem() {}

    public PaymentMethodItem(String accountHolderName, String bankName, String accountNumber, boolean isDefault) {
        this.accountHolderName = accountHolderName;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.isDefault = isDefault;
    }

    // Getters
    public String getAccountHolderName() { return accountHolderName; }
    public String getBankName() { return bankName; }
    public String getAccountNumber() { return accountNumber; }
    public boolean isDefault() { return isDefault; }

    // Setters
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    // Hiển thị dạng: "VCB 0999 888 777"
    public String getDisplayName() {
        return bankName + " " + accountNumber;
    }
}