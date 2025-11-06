package com.example.secondchance.dto.response;

public class CategoryResponse {
  public final String id;
  public final String name;
  public final String iconName; // gmd_* | cmd_*
  
  public CategoryResponse(String id, String name, String iconName) {
    this.id = id;
    this.name = name;
    this.iconName = iconName;
  }
}
