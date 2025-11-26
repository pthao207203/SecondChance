package com.example.secondchance.dto.request;

import com.google.gson.annotations.SerializedName;

public class AddressRequest {
    @SerializedName("label")
    private String label;
    @SerializedName("country")
    private String country;
    @SerializedName("province")
    private String province;
    @SerializedName("ward")
    private String ward;
    @SerializedName("street")
    private String street;
    @SerializedName("name")
    private String name;
    @SerializedName("phone")
    private String phone;
    @SerializedName("isDefault")
    private boolean isDefault;
    @SerializedName("location")
    private Location location;

    public AddressRequest(String name, String phone, String street, String ward, String province, String country, String label, boolean isDefault, double lat, double lng) {
        this.name = name;
        this.phone = phone;
        this.street = street;
        this.ward = ward;
        this.province = province;
        this.country = country;
        this.label = label;
        this.isDefault = isDefault;
        this.location = new Location(lat, lng);
    }

    public static class Location {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;

        public Location(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }
}
