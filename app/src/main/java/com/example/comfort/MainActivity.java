package com.example.comfort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comfort.models.BoothState;
import com.example.comfort.models.HouseState;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private MainApplication app;
    private HouseState houseState;
    private BoothState boothState;

    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView textViewTemperatureOutSide;
    private TextView textViewTemperature;
    private TextView textViewHumidity;
    private TextView textViewTempBooth;
    private TextView textViewTempFloor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        app = (MainApplication) getApplication();

        textViewTemperatureOutSide = findViewById(R.id.textViewTemperatureOutSide);
        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewHumidity = findViewById(R.id.textViewHumidity);
        textViewTempBooth = findViewById(R.id.textViewTempBooth);
        textViewTempFloor = findViewById(R.id.textViewTempFloor);

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        findViewById(R.id.cardViewHouse).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HouseActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.cardViewBooth).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BoothActivity.class);
            startActivity(intent);
        });

        subscribeOnState();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        houseState = null;
        boothState = null;
        app.refreshHouseState();
        app.refreshBoothState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void subscribeOnState() {
        compositeDisposable.add(
                app.getHouseState$()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                houseState -> {
                                    this.houseState = houseState;
                                    updateComponentsState();
                                }
                        )
        );
        compositeDisposable.add(
                app.getBoothState$()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                boothState -> {
                                    this.boothState = boothState;
                                    updateComponentsState();
                                }
                        )
        );
        compositeDisposable.add(
                app.getError$()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                error -> {
                                    if (error != "") {
                                        mSwipeRefreshLayout.setRefreshing(false);
                                        toast(error);
                                    }
                                }
                        )
        );
    }

    private void updateComponentsState() {
        if (houseState == null || boothState == null) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(false);
        textViewTemperatureOutSide.setText(Math.round(boothState.temperatureOutSide) + getString(R.string.celsius));
        textViewTemperature.setText(Math.round(houseState.temperature) + getString(R.string.celsius));
        textViewHumidity.setText(Math.round(houseState.humidity) + getString(R.string.percents));
        textViewTempBooth.setText(Math.round(boothState.temperatureAir) + getString(R.string.celsius));
        textViewTempFloor.setText(Math.round(boothState.temperatureFloor) + getString(R.string.celsius));
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
