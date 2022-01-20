package com.example.comfort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comfort.models.BoothState;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BoothActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private MainApplication app;
    private BoothState boothState;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView textViewTempBooth;
    private TextView textViewTempFloor;
    private TextView textViewLastCheck;
    private TextView textViewHeatOnTime;
    private SwitchCompat switchPower;
    private SwitchCompat switchAutoMode;
    private SwitchCompat switchFloor;
    private SwitchCompat switchHeater;
    private EditText editTemperature;
    private CardView cardViewAutoMode;
    private CardView cardViewTemperature;
    private CardView cardViewLastCheck;
    private CardView cardViewHeatOnTime;
    private CardView cardViewFloor;
    private CardView cardViewHeater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_booth);

        app = (MainApplication) getApplication();

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        textViewTempBooth = findViewById(R.id.textViewTempBooth);
        textViewTempFloor = findViewById(R.id.textViewTempFloor);
        textViewLastCheck = findViewById(R.id.textViewLastCheck);
        textViewHeatOnTime = findViewById(R.id.textViewHeatOnTime);
        switchPower = findViewById(R.id.switchPower);
        switchAutoMode = findViewById(R.id.switchAutoMode);
        switchFloor = findViewById(R.id.switchFloor);
        switchHeater = findViewById(R.id.switchHeater);
        editTemperature = findViewById(R.id.editTemperature);
        cardViewAutoMode = findViewById(R.id.cardViewAutoMode);
        cardViewTemperature = findViewById(R.id.cardViewTemperature);
        cardViewLastCheck = findViewById(R.id.cardViewLastCheck);
        cardViewHeatOnTime = findViewById(R.id.cardViewHeatOnTime);
        cardViewFloor = findViewById(R.id.cardViewFloor);
        cardViewHeater = findViewById(R.id.cardViewHeater);

        switchPower.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (boothState.powerOn == isChecked) {
                return;
            }
            boothState.powerOn = isChecked;
            updateComponentsState();
            putState();
        });

        switchAutoMode.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (boothState.autoMode == isChecked) {
                return;
            }
            boothState.autoMode = isChecked;
            updateComponentsState();
            putState();
        });

        switchFloor.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (boothState.relayFloorOn == isChecked || boothState.autoMode) {
                return;
            }
            boothState.relayFloorOn = isChecked;
            putState();
        });

        switchHeater.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (boothState.relayHeaterOn == isChecked || boothState.autoMode) {
                return;
            }
            boothState.relayHeaterOn = isChecked;
            putState();
        });

        editTemperature.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            switch (actionId){
                case EditorInfo.IME_ACTION_DONE:
                case EditorInfo.IME_ACTION_NEXT:
                case EditorInfo.IME_ACTION_PREVIOUS:
                    onTemperatureChanged();
                    return true;
            }
            return false;
        });

        subscribeOnState();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        app.refreshBoothState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
    }

    private void subscribeOnState() {
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

    private void onTemperatureChanged() {
        String text = editTemperature.getText().toString();
        if (!text.equals("")) {
            try {
                int temperature = Math.min(Math.max(Integer.parseInt(text), 15), 24);
                if (boothState.controlTemperature == temperature) {
                    return;
                }
                boothState.controlTemperature = temperature;
                putState();
            } catch (NumberFormatException e) {}
        }
    }

    private void updateComponentsState() {
        if (boothState == null) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(false);
        textViewTempFloor.setText(Math.round(boothState.temperatureFloor) + getString(R.string.celsius));
        textViewTempBooth.setText(Math.round(boothState.temperatureAir) + getString(R.string.celsius));
        textViewLastCheck.setText(this.millisToHoursMinutesAndSeconds(boothState.lastCheck));
        textViewHeatOnTime.setText(this.millisToHoursMinutesAndSeconds(boothState.heatOnTime));
        editTemperature.setText(String.valueOf(boothState.controlTemperature));

        switchPower.setChecked(boothState.powerOn);
        switchAutoMode.setChecked(boothState.autoMode);
        switchFloor.setChecked(boothState.relayFloorOn);
        switchHeater.setChecked(boothState.relayHeaterOn);

        editTemperature.setEnabled(boothState.powerOn && boothState.autoMode);
        switchFloor.setEnabled(boothState.powerOn && !boothState.autoMode);
        switchHeater.setEnabled(boothState.powerOn && !boothState.autoMode);

        cardViewAutoMode.setVisibility(boothState.powerOn ? View.VISIBLE : View.GONE);
        cardViewTemperature.setVisibility(boothState.powerOn && boothState.autoMode ? View.VISIBLE : View.GONE);
        cardViewLastCheck.setVisibility(boothState.powerOn && boothState.autoMode ? View.VISIBLE : View.GONE);
        cardViewHeatOnTime.setVisibility(boothState.powerOn ? View.VISIBLE : View.GONE);
        cardViewFloor.setVisibility(boothState.powerOn ? View.VISIBLE : View.GONE);
        cardViewHeater.setVisibility(boothState.powerOn ? View.VISIBLE : View.GONE);
    }

    private void putState() {
        app.putBoothState();
    }

    private String  millisToHoursMinutesAndSeconds(int millis) {
        int seconds = (int)Math.floor((millis / 1000) % 60);
        int minutes = (int)Math.floor((millis / (1000 * 60)) % 60);
        int hours = (int)Math.floor((millis / (1000 * 60 * 60)) % 24);

        String strHours = (hours < 10) ? "0" + hours : String.valueOf(hours);
        String strMinutes = (minutes < 10) ? "0" + minutes : String.valueOf(minutes);
        String strSeconds = (seconds < 10) ? "0" + seconds : String.valueOf(seconds);

        return strHours + ":" + strMinutes + ":" + strSeconds;
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
