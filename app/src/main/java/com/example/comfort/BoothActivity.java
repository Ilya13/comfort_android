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

import com.example.comfort.mediators.BoothMediator;
import com.example.comfort.models.BoothState;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BoothActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private BoothState state;
    private final BoothMediator boothMediator = new BoothMediator();
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

        getState();

        switchPower.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (state.powerOn == isChecked) {
                return;
            }
            state.powerOn = isChecked;
            updateComponentsState();
            putState();
        });

        switchAutoMode.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (state.autoMode == isChecked) {
                return;
            }
            state.autoMode = isChecked;
            updateComponentsState();
            putState();
        });

        switchFloor.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (state.relayFloorOn == isChecked || state.autoMode) {
                return;
            }
            state.relayFloorOn = isChecked;
            putState();
        });

        switchHeater.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (state.relayHeaterOn == isChecked || state.autoMode) {
                return;
            }
            state.relayHeaterOn = isChecked;
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.compositeDisposable.dispose();
    }

    private void onTemperatureChanged() {
        String text = editTemperature.getText().toString();
        if (!text.equals("")) {
            try {
                int temperature = Math.min(Math.max(Integer.parseInt(text), 15), 24);
                if (state.controlTemperature == temperature) {
                    return;
                }
                state.controlTemperature = temperature;
                putState();
            } catch (NumberFormatException e) {}
        }
    }

    @Override
    public void onRefresh() {
        getState();
    }

    private void updateComponentsState() {
        if (state == null) {
            return;
        }
        textViewTempFloor.setText(Math.round(state.temperatureFloor) + getString(R.string.celsius));
        textViewTempBooth.setText(Math.round(state.temperatureAir) + getString(R.string.celsius));
        textViewLastCheck.setText(this.millisToHoursMinutesAndSeconds(state.lastCheck));
        textViewHeatOnTime.setText(this.millisToHoursMinutesAndSeconds(state.heatOnTime));
        editTemperature.setText(String.valueOf(state.controlTemperature));

        switchPower.setChecked(state.powerOn);
        switchAutoMode.setChecked(state.autoMode);
        switchFloor.setChecked(state.relayFloorOn);
        switchHeater.setChecked(state.relayHeaterOn);

        editTemperature.setEnabled(state.powerOn && state.autoMode);
        switchFloor.setEnabled(state.powerOn && !state.autoMode);
        switchHeater.setEnabled(state.powerOn && !state.autoMode);

        cardViewAutoMode.setVisibility(state.powerOn ? View.VISIBLE : View.GONE);
        cardViewTemperature.setVisibility(state.powerOn && state.autoMode ? View.VISIBLE : View.GONE);
        cardViewLastCheck.setVisibility(state.powerOn && state.autoMode ? View.VISIBLE : View.GONE);
        cardViewHeatOnTime.setVisibility(state.powerOn ? View.VISIBLE : View.GONE);
        cardViewFloor.setVisibility(state.powerOn ? View.VISIBLE : View.GONE);
        cardViewHeater.setVisibility(state.powerOn ? View.VISIBLE : View.GONE);
    }

    private void getState() {
        mSwipeRefreshLayout.setRefreshing(true);
        compositeDisposable.add(boothMediator.getBoothState()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        boothState -> {
                            mSwipeRefreshLayout.setRefreshing(false);
                            state = boothState;
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

    private void putState() {
        compositeDisposable.add(boothMediator.putBoothState(state)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {},
                        error -> toast(error.getMessage())
                )
        );
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
