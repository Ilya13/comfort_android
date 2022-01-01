package com.example.comfort.api;

import io.reactivex.rxjava3.core.Completable;
import retrofit2.http.GET;

public interface PcApi {
    @GET("/off")
    public Completable off();

    @GET("/off/cancel")
    public Completable cancel();
}
