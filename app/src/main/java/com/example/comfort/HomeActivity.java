package com.example.comfort;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.comfort.models.State;
import com.example.comfort.services.EspService;
import com.example.comfort.services.HomeService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private State state;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Switch switch1;
    private Switch switch2;
    private Switch switch3;
    private Switch switch4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(this);
        switch2 = findViewById(R.id.switch2);
        switch2.setOnCheckedChangeListener(this);
        switch3 = findViewById(R.id.switch3);
        switch3.setOnCheckedChangeListener(this);
        switch4 = findViewById(R.id.switch4);
        switch4.setOnCheckedChangeListener(this);

        Button myBtn = findViewById(R.id.button);
        myBtn.setOnClickListener(this);
        myBtn = findViewById(R.id.button2);
        myBtn.setOnClickListener(this);
        myBtn = findViewById(R.id.button3);
        myBtn.setOnClickListener(this);

        EspService.getInstance().getApi().getState().enqueue(new Callback<State>() {
            @Override
            public void onResponse(Call<State> call, Response<State> response) {
                state = response.body();
                updateSwitchState();
            }

            @Override
            public void onFailure(Call<State> call, Throwable t) {

            }
        });
    }

    @Override
    public void onRefresh() {
        EspService.getInstance().getApi().getState().enqueue(new Callback<State>() {
            @Override
            public void onResponse(Call<State> call, Response<State> response) {
                mSwipeRefreshLayout.setRefreshing(false);
                state = response.body();
                updateSwitchState();
            }

            @Override
            public void onFailure(Call<State> call, Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        byte index = 0;
        switch (buttonView.getId()) {
            case R.id.switch1:
                index = 1;
                break;
            case R.id.switch2:
                index = 2;
                break;
            case R.id.switch3:
                index = 3;
                break;
            case R.id.switch4:
                index = 4;
                break;
        }
        EspService.getInstance().getApi().setState(index, isChecked ? 1 : 0).enqueue(new Callback<State>() {
            @Override
            public void onResponse(Call<State> call, Response<State> response) {
                state = response.body();
            }

            @Override
            public void onFailure(Call<State> call, Throwable t) {
                toast(t.getMessage());
            }
        });
    }

    @Override
    public void onClick(View btn) {
        switch (btn.getId()) {
            case R.id.button:
                HomeService.wakeUp();
                toast("Компьютер включен");
                break;
            case R.id.button2:
                HomeService.getInstance().getApi().off();
                toast("Компьютер выключается");
                break;
            case R.id.button3:
                HomeService.getInstance().getApi().cancel();
                toast("Выключение отменено");
                break;
        }
    }

    private void updateSwitchState() {
        if (state == null) {
            return;
        }
        switch1.setChecked(state.dimmers[0].state == 1);
        switch2.setChecked(state.dimmers[1].state == 1);
        switch3.setChecked(state.dimmers[2].state == 1);
        switch4.setChecked(state.dimmers[3].state == 1);
    }

    private void toast(String text) {
        Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
