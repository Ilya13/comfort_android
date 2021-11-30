package com.example.comfort.services;

import com.example.comfort.models.BoothState;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public interface BoothService {
    Observable<BoothState> getBoothState();

    Completable putBoothState(BoothState body);
}
