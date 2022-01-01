package com.example.comfort.api;

import com.example.comfort.models.HouseRelay;
import com.example.comfort.models.HouseState;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface HouseEspApi {
    @GET("/state")
    public Observable<HouseState> getState();

    @GET("/relay")
    public Completable setRelayState(@Query("n") HouseRelay index, @Query("s") int state);
}
