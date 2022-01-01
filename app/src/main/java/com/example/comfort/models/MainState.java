package com.example.comfort.models;

public class MainState {
    public MainState(HouseState houseState, BoothState boothState) {
        this.houseState = houseState;
        this.boothState = boothState;
    }

    public HouseState houseState;
    public BoothState boothState;
}
