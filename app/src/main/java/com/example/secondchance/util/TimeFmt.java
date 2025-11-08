package com.example.secondchance.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TimeFmt {
  private TimeFmt() {}
  
  private static final Locale VI = new Locale("vi","VN");
  
  // Ví dụ đã dùng trước: "HH:ss, dd/MM/yyyy"
  public static final DateTimeFormatter VN_FULL =
    DateTimeFormatter.ofPattern("HH:ss, dd/MM/yyyy", VI);
  
  // Chỉ ngày trong tháng: "dd"
  public static final DateTimeFormatter DD =
    DateTimeFormatter.ofPattern("dd", VI);
  
  /** ISO-8601 -> "HH:ss, dd/MM/yyyy" */
  public static String isoToVN(String iso) {
    try {
      return Instant.parse(iso).atZone(ZoneId.systemDefault()).format(VN_FULL);
    } catch (Exception e) { return iso != null ? iso : ""; }
  }
  
  /** ISO-8601 -> "dd" */
  public static String isoToDay(String iso) {
    try {
      return Instant.parse(iso).atZone(ZoneId.systemDefault()).format(DD);
    } catch (Exception e) { return ""; }
  }
  
  /** millis -> "dd" (tiện nếu bạn có epoch millis) */
  public static String millisToDay(long epochMillis) {
    try {
      return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(DD);
    } catch (Exception e) { return ""; }
  }
}
