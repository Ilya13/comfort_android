package com.example.comfort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comfort.models.HouseState;
import com.example.comfort.services.PcService;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HouseActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    private MainApplication app;
    private HouseState houseState;
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

        app = (MainApplication) getApplication();

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        textViewTemperature = findViewById(R.id.textViewTemperature);
        textViewHumidity = findViewById(R.id.textViewHumidity);

        livingSwitch = findViewById(R.id.livingSwitch);
        diningSwitch = findViewById(R.id.diningSwitch);
        kitchenSwitch = findViewById(R.id.kitchenSwitch);
        hallwaySwitch = findViewById(R.id.hallwaySwitch);

        livingSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (houseState.relayLivingOn == isChecked) {
                return;
            }
            houseState.relayLivingOn = isChecked;
            updateComponentsState();
            putState();
        });

        diningSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (houseState.relayDiningOn == isChecked) {
                return;
            }
            houseState.relayDiningOn = isChecked;
            updateComponentsState();
            putState();
        });

        kitchenSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (houseState.relayKitchenOn == isChecked) {
                return;
            }
            houseState.relayKitchenOn = isChecked;
            updateComponentsState();
            putState();
        });

        hallwaySwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (houseState.relayHallwayOn == isChecked) {
                return;
            }
            houseState.relayHallwayOn = isChecked;
            updateComponentsState();
            putState();
        });

        Button myBtn = findViewById(R.id.pcOnButton);
        myBtn.setOnClickListener(this);
        myBtn = findViewById(R.id.pcOffbutton);
        myBtn.setOnClickListener(this);
        myBtn = findViewById(R.id.cancelPcOffbutton);
        myBtn.setOnClickListener(this);

        subscribeOnState();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        app.refreshHouseState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.dispose();
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
        if (houseState == null) {
            return;
        }
        mSwipeRefreshLayout.setRefreshing(false);
        textViewTemperature.setText(Math.round(houseState.temperature) + getString(R.string.celsius));
        textViewHumidity.setText(Math.round(houseState.humidity) + getString(R.string.percents));
        livingSwitch.setChecked(houseState.relayLivingOn);
        diningSwitch.setChecked(houseState.relayDiningOn);
        kitchenSwitch.setChecked(houseState.relayKitchenOn);
        hallwaySwitch.setChecked(houseState.relayHallwayOn);
    }

    private void putState() {
        app.putHouseState();
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
