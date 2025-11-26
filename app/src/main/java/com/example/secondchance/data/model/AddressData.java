package com.example.secondchance.data.model;

import com.google.gson.annotations.SerializedName;

public class AddressData {

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("phone")
    private String phone;

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

    @SerializedName("isDefault")
    private boolean isDefault;

    @SerializedName("location")
    private Location location;

    public static class Location {
        @SerializedName("lat")
        public double lat;
        @SerializedName("lng")
        public double lng;
        
        public Location() {}
        
        public Location(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getLabel() {
        return label;
    }

    public String getCountry() {
        return country;
    }

    public String getProvince() {
        return province;
    }

    public String getWard() {
        return ward;
    }

    public String getStreet() {
        return street;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public Location getLocation() {
        return location;
    }
}
