package com.example.comfort.services;

import com.example.comfort.api.CloudApi;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class CloudService {
    private static CloudService mInstance;
    private static final String BASE_URL = "http://139.185.42.223/";
    private Retrofit mRetrofit;

    private CloudService() {mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    public static CloudService getInstance() {
        if (mInstance == null) {
            mInstance = new CloudService();
        }
        return mInstance;
    }

    public CloudApi getApi() {
        return mRetrofit.create(CloudApi.class);
    }
}
