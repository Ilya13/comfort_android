package com.example.comfort.models;

public class HouseState {
    public HouseState() {}

    public HouseState(HouseState state) {
        temperature = state.temperature;
        humidity = state.humidity;
        relayKitchenOn = state.relayKitchenOn;
        relayLivingOn = state.relayLivingOn;
        relayDiningOn = state.relayDiningOn;
        relayHallwayOn = state.relayHallwayOn;
    }

    public float temperature;
    public float humidity;
    public boolean relayKitchenOn;
    public boolean relayLivingOn;
    public boolean relayDiningOn;
    public boolean relayHallwayOn;
}
