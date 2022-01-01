package com.example.comfort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comfort.mediators.HouseMediator;
import com.example.comfort.models.HouseState;
import com.example.comfort.services.PcService;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HouseActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private HouseState state;
    private final HouseMediator houseMediator = new HouseMediator();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView textViewTemperature;
    private TextView textViewHumidity;
    private SwitchCompat livingSwitch;
    private SwitchCompat diningSwitch;
    private SwitchCompat kitchenSwitch;
    private SwitchCompat hallwaySwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_house);

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewHumidity = findViewById(R.id.textViewHumidity);

        livingSwitch = findViewById(R.id.livingSwitch);
        diningSwitch = findViewById(R.id.diningSwitch);
        kitchenSwitch = findViewById(R.id.kitchenSwitch);
        hallwaySwitch = findViewById(R.id.hallwaySwitch);

        livingSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (state.relayLivingOn == isChecked) {
                return;
            }
            state.relayLivingOn = isChecked;
            updateComponentsState();
            putState();
        });

        diningSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (state.relayDiningOn == isChecked) {
                return;
            }
            state.relayDiningOn = isChecked;
            updateComponentsState();
            putState();
        });

        kitchenSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (state.relayKitchenOn == isChecked) {
                return;
            }
            state.relayKitchenOn = isChecked;
            updateComponentsState();
            putState();
        });

        hallwaySwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (state.relayHallwayOn == isChecked) {
                return;
            }
            state.relayHallwayOn = isChecked;
            updateComponentsState();
            putState();
        });

        Button myBtn = findViewById(R.id.pcOnButton);
        myBtn.setOnClickListener(this);
        myBtn = findViewById(R.id.pcOffbutton);
        myBtn.setOnClickListener(this);
        myBtn = findViewById(R.id.cancelPcOffbutton);
        myBtn.setOnClickListener(this);

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

    @Override
    public void onClick(View btn) {
        switch (btn.getId()) {
            case R.id.pcOnButton:
                PcService.getInstance().wakeUp();
                toast("Компьютер включен");
                break;
            case R.id.pcOffbutton:
                PcService.getInstance().getApi().off();
                toast("Компьютер выключается");
                break;
            case R.id.cancelPcOffbutton:
                PcService.getInstance().getApi().cancel();
                toast("Выключение отменено");
                break;
        }
    }

    private void updateComponentsState() {
        if (state == null) {
            return;
        }
        textViewTemperature.setText(Math.round(state.temperature) + getString(R.string.celsius));
        textViewHumidity.setText(Math.round(state.humidity) + getString(R.string.percents));
        livingSwitch.setChecked(state.relayLivingOn);
        diningSwitch.setChecked(state.relayDiningOn);
        kitchenSwitch.setChecked(state.relayKitchenOn);
        hallwaySwitch.setChecked(state.relayHallwayOn);
    }

    private void getState() {
        mSwipeRefreshLayout.setRefreshing(true);
        compositeDisposable.add(houseMediator.getHouseState()
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
        compositeDisposable.add(houseMediator.putHouseState(state)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {},
                        error -> toast(error.getMessage())
                )
        );
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
