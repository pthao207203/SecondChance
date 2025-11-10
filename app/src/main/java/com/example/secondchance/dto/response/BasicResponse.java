package com.example.secondchance.dto.response;

import com.google.gson.annotations.SerializedName;

public class BasicResponse {
  public boolean success;
  @SerializedName("data")
  public Data data;
  
  public static class Data {
    public boolean ok;
  }
}

