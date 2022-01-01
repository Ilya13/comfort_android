package com.example.comfort.models;

import com.google.gson.annotations.SerializedName;

public enum HouseRelay {
    @SerializedName("living")
    LIVING,
    @SerializedName("dining")
    DINING,
    @SerializedName("kitchen")
    KITCHEN,
    @SerializedName("hallway")
    HALLWAY
}
