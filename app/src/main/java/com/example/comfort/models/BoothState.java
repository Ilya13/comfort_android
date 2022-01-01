package com.example.comfort.models;

public class BoothState {
    public BoothState() {}

    public BoothState(BoothState state) {
        temperatureFloor = state.temperatureFloor;
        temperatureAir = state.temperatureAir;
        temperatureOutSide = state.temperatureOutSide;
        powerOn = state.powerOn;
        autoMode = state.autoMode;
        relayFloorOn = state.relayFloorOn;
        relayHeaterOn = state.relayHeaterOn;
        lastCheck = state.lastCheck;
        heatOnTime = state.heatOnTime;
        controlTemperature = state.controlTemperature;
    }

    public float temperatureFloor;
    public float temperatureAir;
    public float temperatureOutSide;
    public boolean powerOn;
    public boolean autoMode;
    public boolean relayFloorOn;
    public boolean relayHeaterOn;
    public int lastCheck;
    public int heatOnTime;
    public int controlTemperature;
}
