package com.example.secondchance.dto.response;

import java.util.List;

public class ProductMetaResponse {
  public boolean success;
  public Data data;
  
  public static class Data {
    public List<Category> categories;
    public List<Brand> brands;
  }
  
  public static class Category {
    public String id;
    public String name;
    public String icon;
    public int order;
    public String parentId;
  }
  
  public static class Brand {
    public String id;
    public String name;
    public String logo;
    public int order;
  }
}
