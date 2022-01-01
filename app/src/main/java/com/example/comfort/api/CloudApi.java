package com.example.comfort.api;

import com.example.comfort.models.BoothState;
import com.example.comfort.models.HouseState;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface CloudApi {
    @GET("/booth/state")
    public Observable<BoothState> getBoothState();

    @PUT("/booth/state")
    public Completable putBoothState(@Body BoothState body);

    @GET("/house/state")
    public Observable<HouseState> getHouseState();

    @PUT("/house/state")
    public Completable putHouseState(@Body HouseState body);
}
