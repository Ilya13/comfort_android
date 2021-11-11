package com.example.comfort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comfort.models.BoothState;
import com.example.comfort.services.BoothEspService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BoothActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private BoothState state;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView textViewTempBooth;
    private TextView textViewTempFloor;
    private TextView textViewLastCheck;
    private TextView textViewHeatOnTime;
    private Switch switchPower;
    private Switch switchAutoMode;
    private Switch switchFloor;
    private Switch switchHeater;
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

        BoothEspService.getInstance().getApi().getState().enqueue(new Callback<BoothState>() {
            @Override
            public void onResponse(Call<BoothState> call, Response<BoothState> response) {
                state = response.body();
                updateComponentsState();
            }

            @Override
            public void onFailure(Call<BoothState> call, Throwable t) {
                toast(t.getMessage());
            }
        });

        switchPower.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if ((state.powerOn == 1 && isChecked) || state.powerOn == 0 && !isChecked) {
                return;
            }
            Callback callback = new Callback<BoothState>() {
                @Override
                public void onResponse(Call<BoothState> call, Response<BoothState> response) {
                    state = response.body();
                    updateComponentsState();
                }

                @Override
                public void onFailure(Call<BoothState> call, Throwable t) {
                    toast(t.getMessage());
                }
            };
            if (isChecked) {
                BoothEspService.getInstance().getApi().powerOn().enqueue(callback);
            } else {
                BoothEspService.getInstance().getApi().powerOff().enqueue(callback);
            }
        });

        switchAutoMode.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if ((state.autoMode == 1 && isChecked) || state.autoMode == 0 && !isChecked) {
                return;
            }
            BoothEspService.getInstance().getApi().setMode(isChecked ? 1 : 0).enqueue(new Callback<BoothState>() {
                @Override
                public void onResponse(Call<BoothState> call, Response<BoothState> response) {
                    state = response.body();
                    updateComponentsState();
                }

                @Override
                public void onFailure(Call<BoothState> call, Throwable t) {
                    toast(t.getMessage());
                }
            });
        });

        switchFloor.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if ((state.relayFloorOn == 1 && isChecked) || state.relayFloorOn == 0 && !isChecked) {
                return;
            }
            if (state.autoMode == 0) {
                BoothEspService.getInstance().getApi().setRelayState("floor", isChecked ? 1 : 0).enqueue(new Callback<BoothState>() {
                    @Override
                    public void onResponse(Call<BoothState> call, Response<BoothState> response) {
                        state = response.body();
                    }

                    @Override
                    public void onFailure(Call<BoothState> call, Throwable t) {
                        toast(t.getMessage());
                    }
                });
            }
        });

        switchHeater.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if ((state.relayHeaterOn == 1 && isChecked) || state.relayHeaterOn == 0 && !isChecked) {
                return;
            }
            if (state.autoMode == 0) {
                BoothEspService.getInstance().getApi().setRelayState("heater", isChecked ? 1 : 0).enqueue(new Callback<BoothState>() {
                    @Override
                    public void onResponse(Call<BoothState> call, Response<BoothState> response) {
                        state = response.body();
                    }

                    @Override
                    public void onFailure(Call<BoothState> call, Throwable t) {
                        toast(t.getMessage());
                    }
                });
            }
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

    private void onTemperatureChanged() {
        String text = editTemperature.getText().toString();
        if (!text.equals("")) {
            try {
                int temperature = Integer.parseInt(text);
                if (temperature >= 16 && temperature <= 24) {
                    if (state.controlTemperature == temperature) {
                        return;
                    }
                    BoothEspService.getInstance().getApi().setTemperature(temperature).enqueue(new Callback<BoothState>() {
                        @Override
                        public void onResponse(Call<BoothState> call, Response<BoothState> response) {
                            state = response.body();
                        }

                        @Override
                        public void onFailure(Call<BoothState> call, Throwable t) {
                            toast(t.getMessage());
                        }
                    });
                }
            } catch (NumberFormatException e) {}
        }
    }

    @Override
    public void onRefresh() {
        BoothEspService.getInstance().getApi().getState().enqueue(new Callback<BoothState>() {
            @Override
            public void onResponse(Call<BoothState> call, Response<BoothState> response) {
                mSwipeRefreshLayout.setRefreshing(false);
                state = response.body();
                updateComponentsState();
            }

            @Override
            public void onFailure(Call<BoothState> call, Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);
                toast(t.getMessage());
            }
        });
    }

    private void updateComponentsState() {
        if (state == null) {
            return;
        }
        textViewTempFloor.setText(state.temperatureFloor + getString(R.string.celsius));
        textViewTempBooth.setText(state.temperatureAir + getString(R.string.celsius));
        textViewLastCheck.setText(this.millisToHoursMinutesAndSeconds(state.lastCheck));
        textViewHeatOnTime.setText(this.millisToHoursMinutesAndSeconds(state.heatOnTime));
        editTemperature.setText(String.valueOf(state.controlTemperature));

        switchPower.setChecked(state.powerOn == 1);
        switchAutoMode.setChecked(state.autoMode == 1);
        switchFloor.setChecked(state.relayFloorOn == 1);
        switchHeater.setChecked(state.relayHeaterOn == 1);

        editTemperature.setEnabled(state.powerOn == 1 && state.autoMode == 1);
        switchFloor.setEnabled(state.powerOn == 1 && state.autoMode == 0);
        switchHeater.setEnabled(state.powerOn == 1 && state.autoMode == 0);

        cardViewAutoMode.setVisibility(state.powerOn == 1 ? View.VISIBLE : View.GONE);
        cardViewTemperature.setVisibility(state.powerOn == 1 && state.autoMode == 1 ? View.VISIBLE : View.GONE);
        cardViewLastCheck.setVisibility(state.powerOn == 1 && state.autoMode == 1 ? View.VISIBLE : View.GONE);
        cardViewHeatOnTime.setVisibility(state.powerOn == 1 ? View.VISIBLE : View.GONE);
        cardViewFloor.setVisibility(state.powerOn == 1 ? View.VISIBLE : View.GONE);
        cardViewHeater.setVisibility(state.powerOn == 1 ? View.VISIBLE : View.GONE);
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
