package com.example.comfort.mediators;

import com.example.comfort.adapters.CloudAdapter;
import com.example.comfort.adapters.HouseEspAdapter;
import com.example.comfort.models.HouseState;
import com.example.comfort.models.NetworkStatus;
import com.example.comfort.monitors.NetworkMonitor;
import com.example.comfort.services.HouseService;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class HouseMediator implements HouseService {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final HouseService cloudAdapter = new CloudAdapter();
    private final HouseEspAdapter houseEspAdapter = new HouseEspAdapter();

    private boolean isInInternalNetwork = true;

    public HouseMediator() {
        compositeDisposable.add(NetworkMonitor.NetworkStatus$.subscribe(state -> {
            isInInternalNetwork = state == NetworkStatus.Internal;
        }));
    }

    private HouseService getAdapter() {
        return isInInternalNetwork ? houseEspAdapter : cloudAdapter;
    }

    @Override
    public Observable<HouseState> getHouseState() {
        return getAdapter().getHouseState().onErrorResumeNext(throwable -> {
            if (isInInternalNetwork && throwable.getMessage().startsWith("failed to connect")) {
                isInInternalNetwork = false;
                return getHouseState();
            }
            throw throwable;
        });
    }

    @Override
    public Completable putHouseState(HouseState body) {
        return getAdapter().putHouseState(body);
    }

    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
