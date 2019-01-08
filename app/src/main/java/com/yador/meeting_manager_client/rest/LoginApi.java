package com.yador.meeting_manager_client.rest;

import com.yador.meeting_manager_client.model.AuthModel;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginApi {

    @POST("public/login")
    Call<AuthModel> login(@Query("username") String username, @Query("password") String password);

    @POST("public/register")
    Call<AuthModel> register(@Query("username") String username, @Query("password") String password);
}
