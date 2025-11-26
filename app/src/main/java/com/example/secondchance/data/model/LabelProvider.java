package com.example.secondchance.data.model;

import java.util.ArrayList;
import java.util.List;

public class LabelProvider {
    public static List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        labels.add("Nhà");
        labels.add("Công ty");
        return labels;
    }
    
    public static String getApiValue(String displayValue) {
        if (displayValue == null) return "house";
        if (displayValue.equalsIgnoreCase("Công ty")) return "company";
        return "house"; // Default to house
    }
    
    public static String getDisplayValue(String apiValue) {
        if (apiValue == null) return "Nhà";
        if (apiValue.equalsIgnoreCase("company")) return "Công ty";
        return "Nhà"; // Default to Nhà
    }
}
