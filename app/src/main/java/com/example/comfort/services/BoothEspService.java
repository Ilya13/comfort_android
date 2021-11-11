package com.example.comfort.services;

import com.example.comfort.api.BoothEspApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BoothEspService {
    private static BoothEspService mInstance;
    private static final String BASE_URL = "http://192.168.31.238";
    private Retrofit mRetrofit;

    private BoothEspService() {mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static BoothEspService getInstance() {
        if (mInstance == null) {
            mInstance = new BoothEspService();
        }
        return mInstance;
    }

    public BoothEspApi getApi() {
        return mRetrofit.create(BoothEspApi.class);
    }
}
