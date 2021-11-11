package com.example.comfort.services;

import com.example.comfort.api.EspApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EspService {
    private static EspService mInstance;
    private static final String BASE_URL = "http://192.168.31.213";
    private Retrofit mRetrofit;

    private EspService() {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static EspService getInstance() {
        if (mInstance == null) {
            mInstance = new EspService();
        }
        return mInstance;
    }

    public EspApi getApi() {
        return mRetrofit.create(EspApi.class);
    }
}
