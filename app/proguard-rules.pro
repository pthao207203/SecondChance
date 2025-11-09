# --- Giữ annotation & generic info cho Gson/Retrofit ---
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations

# --- Retrofit interfaces & HTTP annotations (đừng để R8 lược bỏ) ---
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# --- OkHttp/Okio (giữ tối thiểu, hạn chế dontwarn nếu có thể) ---
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# --- GIỮ NGUYÊN các DTO/Envelope bạn parse JSON ---
# Đổi package cho đúng project của bạn!
-keep class com.example.secondchance.dto.** { *; }
-keep class com.example.secondchance.api.** { *; }

# Nếu bạn đặt các lớp Envelope/Token trong AuthApi (inner classes), giữ cả inner:
-keep class com.example.secondchance.api.AuthApi$** { *; }

# --- Giữ các field có @SerializedName/@Expose để Gson map đúng ---
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}

# (Nếu nhiều model chưa dùng @SerializedName, vẫn keep toàn bộ DTO ở trên nên an toàn)
