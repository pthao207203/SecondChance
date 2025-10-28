package com.example.secondchance.ui.profile;

import java.io.Serializable;

// Implement Serializable để có thể gửi qua lại giữa các fragment
public class AddressItem implements Serializable {
    private String name;
    private String phone;
    private String address;
    private boolean isDefault;

    // (Tạo constructor rỗng cho Firebase/Room sau này)
    public AddressItem() {}

    public AddressItem(String name, String phone, String address, boolean isDefault) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.isDefault = isDefault;
    }

    // --- Getters ---
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public boolean isDefault() { return isDefault; }

    // --- Setters ---
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setAddress(String address) { this.address = address; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
}