package com.example.contapp.network;

import com.example.contapp.models.AuthResponse;
import com.example.contapp.models.Counter;
import com.example.contapp.models.CounterDetailResponse;
import com.example.contapp.models.UserProfileResponse;

import java.util.HashMap;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Multipart;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("auth/register/")
    Call<AuthResponse> register(@Body HashMap<String, String> body);

    @POST("auth/login/")
    Call<AuthResponse> login(@Body HashMap<String, String> body);

    @GET("counters/")
    Call<List<Counter>> getMyCounters(@Query("status") String status);

    @Multipart
    @POST("counters/create/")
    Call<Counter> createCounter(
        @Part("title") RequestBody title,
        @Part("description") RequestBody description,
        @Part("closed_at") RequestBody closedAt,
        @Part MultipartBody.Part image
    );

    @POST("counters/join/")
    Call<Counter> joinCounter(@Body HashMap<String, String> body);

    @GET("counters/{id}/")
    Call<CounterDetailResponse> getCounterDetail(@Path("id") int id);

    @Multipart
    @PUT("counters/{id}/update/")
    Call<Counter> updateCounter(
            @Path("id") int id,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("closed_at") RequestBody closedAt,
            @Part MultipartBody.Part image
    );

    @DELETE("counters/{id}/delete/")
    Call<Void> deleteCounter(@Path("id") int id);

    @POST("counters/{id}/increment/")
    Call<Counter> incrementCounter(@Path("id") int id);

    @GET("auth/profile/")
    Call<UserProfileResponse> getUserProfile();
}
