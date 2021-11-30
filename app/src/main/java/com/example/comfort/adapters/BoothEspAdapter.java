package com.example.comfort.adapters;

import com.example.comfort.api.BoothEspApi;
import com.example.comfort.models.BoothState;
import com.example.comfort.services.BoothEspService;
import com.example.comfort.services.BoothService;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class BoothEspAdapter implements BoothService {
    private BoothState currentState;
    private BoothEspApi getApi() {
        return BoothEspService.getInstance().getApi();
    }

    @Override
    public Observable<BoothState> getBoothState() {
        return getState().doOnNext(boothState -> currentState = new BoothState(boothState));
    }

    @Override
    public Completable putBoothState(BoothState body) {
        if (currentState.powerOn != body.powerOn) {
            currentState.powerOn = body.powerOn;
            return body.powerOn ? powerOn() : powerOff();
        }
        if (currentState.autoMode != body.autoMode) {
            currentState.autoMode = body.autoMode;
            return body.autoMode ? autoModeOn() : autoModeOff();
        }
        if (currentState.controlTemperature != body.controlTemperature) {
            currentState.controlTemperature = body.controlTemperature;
            return setTemperature(body.controlTemperature);
        }
        if (currentState.relayFloorOn != body.relayFloorOn) {
            currentState.relayFloorOn = body.relayFloorOn;
            return body.relayFloorOn ? floorOn() : floorOff();
        }
        if (currentState.relayHeaterOn != body.relayHeaterOn) {
            currentState.relayHeaterOn = body.relayHeaterOn;
            return body.relayHeaterOn ? heaterOn() : heaterOff();
        }
        return null;
    }

    private Observable<BoothState> getState() {
        return getApi().getState();
    }

    private Completable powerOn() {
        return getApi().powerOn();
    }

    private Completable powerOff() {
        return getApi().powerOff();
    }

    private Completable autoModeOn() {
        return getApi().setMode(1);
    }

    private Completable autoModeOff() {
        return getApi().setMode(0);
    }

    private Completable setTemperature(int temperature) {
        return getApi().setTemperature(temperature);
    }

    private Completable floorOn() {
        return getApi().setRelayState("floor", 1);
    }

    private Completable floorOff() {
        return getApi().setRelayState("floor", 0);
    }

    private Completable heaterOn() {
        return getApi().setRelayState("heater", 1);
    }

    private Completable heaterOff() {
        return getApi().setRelayState("heater", 0);
    }
}
