package com.example.comfort.models;

public class WidgetState {
    public WidgetState(HouseState houseState, BoothState boothState) {
        this.houseState = houseState;
        this.boothState = boothState;
    }

    public HouseState houseState;
    public BoothState boothState;
}
