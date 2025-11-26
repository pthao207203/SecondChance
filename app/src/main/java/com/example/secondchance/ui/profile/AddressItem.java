package com.example.secondchance.ui.profile;

import java.io.Serializable;

// Implement Serializable để có thể gửi qua lại giữa các fragment
public class AddressItem implements Serializable {
    private String id;
    private String name;
    private String phone;
    private String street;
    private String ward;
    private String province;
    private String country;
    private String label;
    private boolean isDefault;
    private double lat;
    private double lng;

    // (Tạo constructor rỗng cho Firebase/Room sau này)
    public AddressItem() {}

    public AddressItem(String name, String phone, String street, boolean isDefault) {
        this.name = name;
        this.phone = phone;
        this.street = street;
        this.isDefault = isDefault;
        // Set defaults based on API example to ensure successful request without dropdowns
        this.country = "VietNam";
        this.label = "house";
        this.ward = "Linh Xuân"; 
        this.province = "Hồ Chí Minh";
        // Default location from example
        this.lat = 10.776889;
        this.lng = 106.700806;
    }

    public AddressItem(String id, String name, String phone, String street, String ward, String province, String country, String label, boolean isDefault, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.street = street;
        this.ward = ward;
        this.province = province;
        this.country = country;
        this.label = label;
        this.isDefault = isDefault;
        this.lat = lat;
        this.lng = lng;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getStreet() { return street; }
    public String getWard() { return ward; }
    public String getProvince() { return province; }
    public String getCountry() { return country; }
    public String getLabel() { return label; }
    public boolean isDefault() { return isDefault; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setStreet(String street) { this.street = street; }
    public void setWard(String ward) { this.ward = ward; }
    public void setProvince(String province) { this.province = province; }
    public void setCountry(String country) { this.country = country; }
    public void setLabel(String label) { this.label = label; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }

    public String getAddress() {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isEmpty()) sb.append(street);
        if (ward != null && !ward.isEmpty()) sb.append(", ").append(ward);
        if (province != null && !province.isEmpty()) sb.append(", ").append(province);
        return sb.toString();
    }
}
