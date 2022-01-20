package com.example.comfort;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.comfort.mediators.BoothMediator;
import com.example.comfort.mediators.HouseMediator;
import com.example.comfort.models.BoothState;
import com.example.comfort.models.HouseState;
import com.example.comfort.models.WidgetState;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class ComfortWidget extends AppWidgetProvider {
    private final static HouseMediator houseMediator = new HouseMediator();
    private final static BoothMediator boothMediator = new BoothMediator();
    private final static CompositeDisposable compositeDisposable = new CompositeDisposable();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, WidgetState state) {

        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                /* context = */ context,
                /* requestCode = */ 0,
                /* intent = */ mainIntent,
                /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent houseIntent = new Intent(context, HouseActivity.class);
        TaskStackBuilder houseStackBuilder = TaskStackBuilder.create(context);
        houseStackBuilder.addParentStack(MainActivity.class);
        houseStackBuilder.addNextIntent(houseIntent);
        PendingIntent housePendingIntent =
                houseStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent boothIntent = new Intent(context, BoothActivity.class);
        TaskStackBuilder boothStackBuilder = TaskStackBuilder.create(context);
        boothStackBuilder.addParentStack(MainActivity.class);
        boothStackBuilder.addNextIntent(boothIntent);
        PendingIntent boothPendingIntent =
                boothStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.comfort_widget);

        views.setTextViewText(R.id.textViewTemperatureOutSide, Math.round(state.boothState.temperatureOutSide) + context.getString(R.string.celsius));
        views.setTextViewText(R.id.textViewTemperature, Math.round(state.houseState.temperature) + context.getString(R.string.celsius));
        views.setTextViewText(R.id.textViewTempBooth, Math.round(state.boothState.temperatureAir) + context.getString(R.string.celsius));

        views.setOnClickPendingIntent(R.id.layoutMain, mainPendingIntent);
        views.setOnClickPendingIntent(R.id.layoutHouse, housePendingIntent);
        views.setOnClickPendingIntent(R.id.layoutBooth, boothPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static Observable<WidgetState> getState() {
        Observable<HouseState> houseStateObservable = houseMediator.getHouseState()
                .observeOn(AndroidSchedulers.mainThread());
        Observable<BoothState> boothStateObservable = boothMediator.getBoothState()
                .observeOn(AndroidSchedulers.mainThread());

        return Observable.zip(houseStateObservable, boothStateObservable, WidgetState::new);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        compositeDisposable.add(
                getState().subscribe(
                        state -> {
                            for (int appWidgetId : appWidgetIds) {
                                updateAppWidget(context, appWidgetManager, appWidgetId, state);
                            }
                        }
                )
        );
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        compositeDisposable.dispose();
    }
}