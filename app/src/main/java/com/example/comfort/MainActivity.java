package com.example.comfort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comfort.mediators.BoothMediator;
import com.example.comfort.mediators.HouseMediator;
import com.example.comfort.models.BoothState;
import com.example.comfort.models.HouseState;
import com.example.comfort.models.MainState;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private MainState state;
    private final HouseMediator houseMediator = new HouseMediator();
    private final BoothMediator boothMediator = new BoothMediator();
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

        getState();
    }

    @Override
    public void onRefresh() {
        getState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
        houseMediator.onDestroy();
    }

    private void getState() {
        mSwipeRefreshLayout.setRefreshing(true);
        Observable<HouseState> houseStateObservable = houseMediator.getHouseState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        Observable<BoothState> boothStateObservable = boothMediator.getBoothState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        compositeDisposable.add(
                Observable.zip(houseStateObservable, boothStateObservable, MainState::new)
                .subscribe(
                        mainState -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            state = mainState;
                            updateComponentsState();
                        },
                        error -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            if (error instanceof CompositeException && ((CompositeException) error).getExceptions().size() > 0) {
                                error = ((CompositeException) error).getExceptions().get(0);
                            }
                            toast(error.getMessage());
                        }
                )
        );
    }

    private void updateComponentsState() {
        if (state == null) {
            return;
        }
        textViewTemperatureOutSide.setText(Math.round(state.boothState.temperatureOutSide) + getString(R.string.celsius));
        textViewTemperature.setText(Math.round(state.houseState.temperature) + getString(R.string.celsius));
        textViewHumidity.setText(Math.round(state.houseState.humidity) + getString(R.string.percents));
        textViewTempBooth.setText(Math.round(state.boothState.temperatureAir) + getString(R.string.celsius));;
        textViewTempFloor.setText(Math.round(state.boothState.temperatureFloor) + getString(R.string.celsius));;
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
