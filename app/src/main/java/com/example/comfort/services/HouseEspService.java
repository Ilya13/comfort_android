package com.example.comfort.services;

import com.example.comfort.api.HouseEspApi;
import com.example.comfort.api.PcApi;
import com.example.comfort.factories.EnumRetrofitConverterFactory;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HouseEspService {
    private static HouseEspService mInstance;
    private static final String BASE_URL = "http://192.168.31.117";
    private final Retrofit mRetrofit;

    private HouseEspService() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(new EnumRetrofitConverterFactory())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    public static HouseEspService getInstance() {
        if (mInstance == null) {
            mInstance = new HouseEspService();
        }
        return mInstance;
    }

    public HouseEspApi getApi() {
        return mRetrofit.create(HouseEspApi.class);
    }
}
