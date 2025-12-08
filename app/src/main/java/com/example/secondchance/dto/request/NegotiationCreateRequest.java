package com.example.secondchance.dto.request;

public class NegotiationCreateRequest {
  private long offeredPrice;
  private String message;
  
  public NegotiationCreateRequest(long offeredPrice, String message) {
    this.offeredPrice = offeredPrice;
    this.message = message;
  }
  
  public long getOfferedPrice() { return offeredPrice; }
  public String getMessage() { return message; }
}
