package com.example.comfort.api;

import com.example.comfort.models.State;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EspApi {
    @GET("/state")
    public Call<State> getState();

    @GET("/dimmer")
    public Call<State> setState(@Query("i") int index, @Query("s") int state);
}
