package com.example.secondchance.data.remote;

import com.example.secondchance.dto.request.MessageSendRequestDto;
import com.example.secondchance.dto.response.ConversationListResponseDto;
import com.example.secondchance.dto.response.MessageListResponseDto;
import com.example.secondchance.dto.response.MessageSendResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MessageApi {
  
  @GET("/api/message/conversations")
  Call<ConversationListResponseDto> getConversations();
  
  @GET("/api/message/with/{userId}")
  Call<MessageListResponseDto> getMessagesWith(
    @Path("userId") String userId,
    @Query("page") int page,
    @Query("pageSize") int pageSize
  );
  
  @POST("/api/message")
  Call<MessageSendResponseDto> sendMessage(
    @Body MessageSendRequestDto body
  );
}
