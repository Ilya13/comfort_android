package com.example.comfort.mediators;

import com.example.comfort.adapters.BoothEspAdapter;
import com.example.comfort.adapters.CloudAdapter;
import com.example.comfort.models.BoothState;
import com.example.comfort.models.NetworkStatus;
import com.example.comfort.monitors.NetworkMonitor;
import com.example.comfort.services.BoothService;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class BoothMediator implements BoothService {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final BoothService cloudAdapter = new CloudAdapter();
    private final BoothService boothEspAdapter = new BoothEspAdapter();

    private boolean isInInternalNetwork = true;

    public BoothMediator() {
        compositeDisposable.add(NetworkMonitor.NetworkStatus$.subscribe(state -> {
            isInInternalNetwork = state == NetworkStatus.Internal;
        }));
    }

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

    public void onDestroy() {
        compositeDisposable.dispose();
    }
}
