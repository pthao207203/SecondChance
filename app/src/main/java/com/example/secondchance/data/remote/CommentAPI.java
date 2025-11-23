// data/remote/CommentAPI.java
package com.example.secondchance.data.remote;

import com.example.secondchance.ui.comment.Comment;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.List;

/**
 * API lấy danh sách comment của một shop (seller)
 * Style giống hệt AuthApi: dùng inner class + @SerializedName
 */
public interface CommentAPI {

    @GET("/api/users/{id}/comments")
    Call<GetCommentsEnvelope> getSellerComments(@Path("id") String sellerId);

    class GetCommentsEnvelope {
        @com.google.gson.annotations.SerializedName("success")
        public boolean success;

        @com.google.gson.annotations.SerializedName("data")
        public Data data;

        @com.google.gson.annotations.SerializedName("meta")
        public Object meta; // không dùng tới

        public static class Data {
            @com.google.gson.annotations.SerializedName("comments")
            public List<Comment> comments;
        }

        @SerializedName("data")
        /** Helper: lấy list comment dù backend bọc trong data hay trả thẳng */
        public List<Comment> getComments() {
            return data != null && data.comments != null ? data.comments : java.util.Collections.emptyList();
        }
    }
}
