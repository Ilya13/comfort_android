package com.example.comfort;

import android.app.PendingIntent;
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

import java.util.function.Function;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * Implementation of App Widget functionality.
 */
public class HouseWidget extends AppWidgetProvider {
    private static final String ACTION_LIVING = "ACTION_LIVING";
    private static final String ACTION_DINING = "ACTION_DINING";
    private static final String ACTION_KITCHEN = "ACTION_KITCHEN";
    private static final String ACTION_HALLWAY = "ACTION_HALLWAY";
    private final static HouseMediator houseMediator = new HouseMediator();
    private final static CompositeDisposable compositeDisposable = new CompositeDisposable();

    static PendingIntent getIntentByAction(Context context, String action) {
        // Construct an Intent which is pointing this class.
        Intent intent = new Intent(context, HouseWidget.class);
        intent.setAction(action);
        // And this time we are sending a broadcast with getBroadcast
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.house_widget);

        views.setOnClickPendingIntent(R.id.imageViewLiving, getIntentByAction(context, ACTION_LIVING));
        views.setOnClickPendingIntent(R.id.imageViewDining, getIntentByAction(context, ACTION_DINING));
        views.setOnClickPendingIntent(R.id.imageViewKitchen, getIntentByAction(context, ACTION_KITCHEN));
        views.setOnClickPendingIntent(R.id.imageViewHallway, getIntentByAction(context, ACTION_HALLWAY));
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        // Enter relevant functionality for when the last widget is disabled
        compositeDisposable.dispose();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Observable<HouseState> stateObservable = houseMediator.getHouseState()
                .observeOn(AndroidSchedulers.mainThread());

        if (ACTION_LIVING.equals(intent.getAction())) {
            stateObservable = stateObservable.map(state -> {
                state.relayLivingOn = !state.relayLivingOn;
                return state;
            });
        } else if (ACTION_DINING.equals(intent.getAction())) {
            stateObservable = stateObservable.map(state -> {
                state.relayDiningOn = !state.relayDiningOn;
                return state;
            });
        } else if (ACTION_KITCHEN.equals(intent.getAction())) {
            stateObservable = stateObservable.map(state -> {
                state.relayKitchenOn = !state.relayKitchenOn;
                return state;
            });
        } else if (ACTION_HALLWAY.equals(intent.getAction())) {
            stateObservable = stateObservable.map(state -> {
                state.relayHallwayOn = !state.relayHallwayOn;
                return state;
            });
        }
        compositeDisposable.add(
                stateObservable.switchMapCompletable(state -> houseMediator.putHouseState(state)).subscribe()
        );
    }
}