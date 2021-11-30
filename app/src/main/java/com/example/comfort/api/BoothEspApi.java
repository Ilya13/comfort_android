package com.example.comfort.api;

import com.example.comfort.models.BoothState;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BoothEspApi {
    @GET("/state")
    public Observable<BoothState> getState();

    @GET("/on")
    public Completable powerOn();

    @GET("/off")
    public Completable powerOff();

    @GET("/mode")
    public Completable setMode(@Query("v") int mode);

    @GET("/temperature")
    public Completable setTemperature(@Query("v") int temperature);

    @GET("/relay")
    public Completable setRelayState(@Query("n") String name, @Query("s") int state);
}
