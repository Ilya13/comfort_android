package com.example.comfort.adapters;

import com.example.comfort.api.HouseEspApi;
import com.example.comfort.models.HouseRelay;
import com.example.comfort.models.HouseState;
import com.example.comfort.services.HouseEspService;
import com.example.comfort.services.HouseService;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public class HouseEspAdapter implements HouseService {
    private HouseState currentState;
    private HouseEspApi getApi() {
        return HouseEspService.getInstance().getApi();
    }

    @Override
    public Observable<HouseState> getHouseState() {
        return getState().doOnNext(houseState -> currentState = new HouseState(houseState));
    }

    @Override
    public Completable putHouseState(HouseState body) {
        if (currentState.relayLivingOn != body.relayLivingOn) {
            currentState.relayLivingOn = body.relayLivingOn;
            return body.relayLivingOn ? livingOn() : livingOff();
        }
        if (currentState.relayDiningOn != body.relayDiningOn) {
            currentState.relayDiningOn = body.relayDiningOn;
            return body.relayDiningOn ? diningOn() : diningOff();
        }
        if (currentState.relayKitchenOn != body.relayKitchenOn) {
            currentState.relayKitchenOn = body.relayKitchenOn;
            return body.relayKitchenOn ? kitchenOn() : kitchenOff();
        }
        if (currentState.relayHallwayOn != body.relayHallwayOn) {
            currentState.relayHallwayOn = body.relayHallwayOn;
            return body.relayHallwayOn ? hallwayOn() : hallwayOff();
        }
        return null;
    }

    private Observable<HouseState> getState() {
        return getApi().getState();
    }

    private Completable livingOn() {
        return getApi().setRelayState(HouseRelay.LIVING, 1);
    }

    private Completable livingOff() {
        return getApi().setRelayState(HouseRelay.LIVING, 0);
    }

    private Completable diningOn() {
        return getApi().setRelayState(HouseRelay.DINING, 1);
    }

    private Completable diningOff() {
        return getApi().setRelayState(HouseRelay.DINING, 0);
    }

    private Completable kitchenOn() {
        return getApi().setRelayState(HouseRelay.KITCHEN, 1);
    }

    private Completable kitchenOff() {
        return getApi().setRelayState(HouseRelay.KITCHEN, 0);
    }

    private Completable hallwayOn() {
        return getApi().setRelayState(HouseRelay.HALLWAY, 1);
    }

    private Completable hallwayOff() {
        return getApi().setRelayState(HouseRelay.HALLWAY, 0);
    }
}
