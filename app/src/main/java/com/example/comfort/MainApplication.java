package com.example.comfort;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;

import com.example.comfort.mediators.BoothMediator;
import com.example.comfort.mediators.HouseMediator;
import com.example.comfort.models.BoothState;
import com.example.comfort.models.HouseState;
import com.example.comfort.monitors.NetworkMonitor;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class MainApplication extends Application {
    private final HouseMediator houseMediator = new HouseMediator();
    private final BoothMediator boothMediator = new BoothMediator();
    private final BehaviorSubject<String> error$ = BehaviorSubject.create();
    private final BehaviorSubject<HouseState> houseState$ = BehaviorSubject.create();
    private final BehaviorSubject<BoothState> boothState$ = BehaviorSubject.create();

    public Observable<String> getError$() {
        return error$;
    }

    public Observable<HouseState> getHouseState$() {
        return houseState$;
    }

    public Observable<BoothState> getBoothState$() {
        return boothState$;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ConnectivityManager connectivityDispatcher = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityDispatcher != null) {
            NetworkRequest networkRequest = new NetworkRequest.Builder().build();
            connectivityDispatcher.registerNetworkCallback(networkRequest, new NetworkMonitor());
        }
    }

    public void refreshHouseState() {
        error$.onNext("");
        houseMediator.getHouseState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        houseState -> {
                            houseState$.onNext(houseState);
                        },
                        error -> {
                            error$.onNext(getErrorMessage(error));
                        }
                );
    }

    public void refreshBoothState() {
        error$.onNext("");
        boothMediator.getBoothState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        boothState -> {
                            boothState$.onNext(boothState);
                        },
                        error -> {
                            error$.onNext(getErrorMessage(error));
                        }
                );
    }

    public void putHouseState() {
        houseMediator.putHouseState(houseState$.getValue())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {},
                        error -> {
                            error$.onNext(getErrorMessage(error));
                        }
                );
    }

    public void putBoothState() {
        boothMediator.putBoothState(boothState$.getValue())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {},
                        error -> {
                            error$.onNext(getErrorMessage(error));
                        }
                );
    }

    private String getErrorMessage(Throwable error) {
        if (error instanceof CompositeException && ((CompositeException) error).getExceptions().size() > 0) {
            error = ((CompositeException) error).getExceptions().get(0);
        }
        return error.getMessage();
    }
}
