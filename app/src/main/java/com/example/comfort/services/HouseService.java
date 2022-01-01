package com.example.comfort.services;

import com.example.comfort.models.HouseState;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public interface HouseService {
    Observable<HouseState> getHouseState();

    Completable putHouseState(HouseState body);
}
