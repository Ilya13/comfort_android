package com.example.comfort.api;

import com.example.comfort.models.BoothState;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BoothEspApi {
    @GET("/state")
    public Call<BoothState> getState();

    @GET("/on")
    public Call<BoothState> powerOn();

    @GET("/off")
    public Call<BoothState> powerOff();

    @GET("/mode")
    public Call<BoothState> setMode(@Query("v") int mode);

    @GET("/temperature")
    public Call<BoothState> setTemperature(@Query("v") int temperature);

    @GET("/relay")
    public Call<BoothState> setRelayState(@Query("n") String name, @Query("s") int state);
}
