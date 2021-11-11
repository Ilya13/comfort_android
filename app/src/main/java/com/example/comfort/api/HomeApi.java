package com.example.comfort.api;

import retrofit2.Call;
import retrofit2.http.GET;

public interface HomeApi {
    @GET("/off")
    public Call<String> off();

    @GET("/off/cancel")
    public Call<String> cancel();
}

