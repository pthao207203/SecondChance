package com.example.secondchance.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.secondchance.data.remote.CloudinaryApi;
import com.example.secondchance.dto.response.CloudinarySignatureResponse;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;

public class CloudinaryUploader {
  
  private static final String TAG = "CloudinaryUploader";
  
  // Client riêng cho Cloudinary (không xài baseUrl 10.0.2.2)
  private static final OkHttpClient cloudinaryClient = new OkHttpClient.Builder()
    .addInterceptor(new HttpLoggingInterceptor()
      .setLevel(HttpLoggingInterceptor.Level.BODY))
    .build();
  
  /**
   * Upload danh sách ảnh lên Cloudinary.
   * @param ctx context để mở Uri
   * @param uris danh sách Uri được chọn
   * @param folder folder BE dùng để ký (vd: "NT118/products" hoặc "NT118/avatar"...)
   * @param cloudinaryApi RetrofitProvider.cloudinary()
   */
  @NonNull
  public static List<String> uploadImages(
    @NonNull Context ctx,
    @NonNull List<Uri> uris,
    String folder,
    @NonNull CloudinaryApi cloudinaryApi
  ) throws IOException {
    
    List<String> urls = new ArrayList<>();
    if (uris.isEmpty()) return urls;
    
    // 1. Xin chữ ký từ BE
    Call<CloudinarySignatureResponse> call = cloudinaryApi.getSignature(folder);
    retrofit2.Response<CloudinarySignatureResponse> sigRes = call.execute();
    if (!sigRes.isSuccessful() || sigRes.body() == null) {
      throw new IOException("Get signature failed: " + sigRes.code());
    }
    
    CloudinarySignatureResponse sig = sigRes.body();
    String effectiveFolder = sig.folder != null ? sig.folder : folder;
    
    // 2. Upload từng ảnh
    for (Uri uri : uris) {
      String url = uploadSingle(ctx, uri, sig, effectiveFolder);
      if (url != null) {
        urls.add(url);
      }
    }
    
    return urls;
  }
  
  private static String uploadSingle(
    Context ctx,
    Uri uri,
    CloudinarySignatureResponse sig,
    String folder
  ) throws IOException {
    InputStream inputStream = ctx.getContentResolver().openInputStream(uri);
    if (inputStream == null) {
      throw new IOException("Cannot open uri: " + uri);
    }
    
    byte[] bytes = readBytes(inputStream);
    inputStream.close();
    
    MediaType mediaType = MediaType.parse("image/*");
    MultipartBody.Builder mb = new MultipartBody.Builder()
      .setType(MultipartBody.FORM)
      .addFormDataPart("file", "image.jpg",
        okhttp3.RequestBody.create(bytes, mediaType))
      .addFormDataPart("api_key", sig.apiKey)
      .addFormDataPart("timestamp", String.valueOf(sig.timestamp))
      .addFormDataPart("signature", sig.signature);
    
    if (folder != null && !folder.isEmpty()) {
      mb.addFormDataPart("folder", folder);
    }
    
    MultipartBody requestBody = mb.build();
    
    String uploadUrl = "https://api.cloudinary.com/v1_1/" +
      sig.cloudName + "/image/upload";
    
    Request request = new Request.Builder()
      .url(uploadUrl)
      .post(requestBody)
      .build();
    
    Response resp = cloudinaryClient.newCall(request).execute();
    if (!resp.isSuccessful()) {
      String err = resp.body() != null ? resp.body().string() : "Unknown error";
      resp.close();
      throw new IOException("Upload image failed: " + err);
    }
    
    String body = resp.body() != null ? resp.body().string() : "{}";
    resp.close();
    
    try {
      JSONObject obj = new JSONObject(body);
      String secureUrl = obj.optString("secure_url", null);
      Log.d(TAG, "Uploaded image url: " + secureUrl);
      return secureUrl;
    } catch (Exception e) {
      throw new IOException("Parse Cloudinary JSON error: " + e.getMessage(), e);
    }
  }
  
  private static byte[] readBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    int nRead;
    byte[] data = new byte[4096];
    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }
    buffer.flush();
    return buffer.toByteArray();
  }
}
