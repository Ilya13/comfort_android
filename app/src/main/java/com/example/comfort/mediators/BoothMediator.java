package com.example.comfort.mediators;

import com.example.comfort.adapters.BoothEspAdapter;
import com.example.comfort.adapters.CloudAdapter;
import com.example.comfort.models.BoothState;
import com.example.comfort.services.BoothService;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class BoothMediator implements BoothService {
    private final BoothService cloudAdapter = new CloudAdapter();
    private final BoothService boothEspAdapter = new BoothEspAdapter();

    private boolean isInInternalNetwork = true;

    private BoothService getAdapter() {
        return isInInternalNetwork ? boothEspAdapter : cloudAdapter;
    }

    @Override
    public Observable<BoothState> getBoothState() {
        return getAdapter().getBoothState().onErrorResumeNext(throwable -> {
            if (isInInternalNetwork && throwable.getMessage().startsWith("failed to connect")) {
                isInInternalNetwork = false;
                return getBoothState();
            }
            throw throwable;
        });
    }

    @Override
    public Completable putBoothState(BoothState body) {
        return getAdapter().putBoothState(body);
    }
}
