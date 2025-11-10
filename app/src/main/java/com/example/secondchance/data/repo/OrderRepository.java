package com.example.secondchance.data.repo;

import androidx.annotation.NonNull;
import com.example.secondchance.data.model.Order;
import com.example.secondchance.data.model.OrderWrapper;
import com.example.secondchance.data.remote.OrderApi;
import com.example.secondchance.data.remote.RetrofitProvider;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;

import com.example.secondchance.dto.response.BasicResponse;
import com.example.secondchance.dto.response.OrderDetailResponse;
public class OrderRepository {

    private OrderApi orderApi;

    public interface RepoCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    public OrderRepository() {

        this.orderApi = RetrofitProvider.order();
    }
    public void cancelOrder(String orderId, RepoCallback<Void> callback) {
        orderApi.cancelOrder(orderId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(@NonNull Call<BasicResponse> call,
                                   @NonNull Response<BasicResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code());
                    return;
                }
                BasicResponse body = response.body();
                if (body == null) {
                    callback.onError("Phản hồi rỗng từ server");
                    return;
                }
                if (body.success && body.data != null && body.data.ok) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Hủy đơn thất bại");
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Lỗi mạng");
            }
        });
    }

    public void fetchOrders(String status, RepoCallback<List<OrderWrapper>> callback) {
        orderApi.getOrdersByStatus(status).enqueue(new Callback<OrderApi.OrderListEnvelope>() {
            @Override
            public void onResponse(@NonNull Call<OrderApi.OrderListEnvelope> call, @NonNull Response<OrderApi.OrderListEnvelope> response) {

                if (response.isSuccessful() &&
                        response.body() != null &&
                        response.body().success &&
                        response.body().data != null &&
                        response.body().data.orders != null)
                {

                    List<com.example.secondchance.data.model.OrderWrapper> wrappedList = response.body().data.orders;

                    List<OrderWrapper> unwrappedList = new ArrayList<>();

                    for (com.example.secondchance.data.model.OrderWrapper wrapper : wrappedList) {
                        if (wrapper != null && wrapper.order != null) {

                            wrapper.order.id = wrapper.id;

                            unwrappedList.add(wrapper);
                        }
                    }

                    callback.onSuccess(unwrappedList);

                } else {
                    String errorMsg = "Lỗi tải đơn hàng";
                    callback.onError(errorMsg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<OrderApi.OrderListEnvelope> call, @NonNull Throwable t) {
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void getOrderDetails(String orderId, RepoCallback<OrderDetailResponse.Data> callback) {
        orderApi.getOrderDetail(orderId).enqueue(new Callback<OrderDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<OrderDetailResponse> call, @NonNull Response<OrderDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    callback.onSuccess(response.body().data);
                } else {
                    callback.onError("Lỗi tải chi tiết đơn hàng");
                }
            }
            @Override
            public void onFailure(@NonNull Call<OrderDetailResponse> call, @NonNull Throwable t) {
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void createReturnRequest(String orderId, OrderApi.ReturnRequestBody body, RepoCallback<Void> callback) {
        orderApi.createReturnRequest(orderId, body).enqueue(new Callback<OrderApi.BaseEnvelope>() {
            @Override
            public void onResponse(@NonNull Call<OrderApi.BaseEnvelope> call, @NonNull Response<OrderApi.BaseEnvelope> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    callback.onSuccess(null); // Thành công
                } else {

                    String errorMsg = "Gửi yêu cầu thất bại (Không rõ lý do)";
                    if (response.body() != null && response.body().message != null) {

                        errorMsg = response.body().message;
                    } else if (response.errorBody() != null) {

                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {}
                    }
                    callback.onError(errorMsg);

                }
            }

            @Override
            public void onFailure(@NonNull Call<OrderApi.BaseEnvelope> call, @NonNull Throwable t) {
                callback.onError("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}