// data/repository/CommentRepository.java
package com.example.secondchance.data.repo;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;

import com.example.secondchance.data.remote.CommentAPI;
import com.example.secondchance.data.remote.RetrofitProvider;
import com.example.secondchance.ui.comment.Comment;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentRepository {

    private final CommentAPI api;

    public CommentRepository() {
        this.api = RetrofitProvider.getRetrofit().create(CommentAPI.class);
    }

    /**
     * Gọi API lấy danh sách comment của seller
     */
    public void getSellerComments(String sellerId, MutableLiveData<List<Comment>> liveData) {
        Log.d("CommentRepo", "Fetching comments for seller: " + sellerId);

        api.getSellerComments(sellerId).enqueue(new Callback<CommentAPI.GetCommentsEnvelope>() {
            @Override
            public void onResponse(Call<CommentAPI.GetCommentsEnvelope> call,
                                   Response<CommentAPI.GetCommentsEnvelope> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comment> comments = response.body().getComments();
                    Log.d("CommentRepo", "API success: " + comments.size() + " comments");
                    liveData.setValue(comments);
                } else {
                    Log.w("CommentRepo", "API returned empty or error: " + response.code());
                    liveData.setValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<CommentAPI.GetCommentsEnvelope> call, Throwable t) {
                Log.e("CommentRepo", "API failed: " + t.getMessage(), t);
                liveData.setValue(Collections.emptyList());
            }
        });
    }
}
