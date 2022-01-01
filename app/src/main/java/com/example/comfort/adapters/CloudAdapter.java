package com.example.comfort.adapters;

import com.example.comfort.models.BoothState;
import com.example.comfort.models.HouseState;
import com.example.comfort.services.BoothService;
import com.example.comfort.services.CloudService;
import com.example.comfort.services.HouseService;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class CloudAdapter implements BoothService, HouseService {
    @Override
    public Observable<BoothState> getBoothState() {
        return CloudService.getInstance().getApi().getBoothState();
    }

    @Override
    public Completable putBoothState(BoothState body) {
        return CloudService.getInstance().getApi().putBoothState(body);
    }

    @Override
    public Observable<HouseState> getHouseState() {
        return CloudService.getInstance().getApi().getHouseState();
    }

    @Override
    public Completable putHouseState(HouseState body) {
        return CloudService.getInstance().getApi().putHouseState(body);
    }
}
