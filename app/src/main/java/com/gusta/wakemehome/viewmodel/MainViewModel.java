package com.gusta.wakemehome.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.Geofence;
import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<AlarmEntry>> alarms;
    private Observer <List<AlarmEntry>> observer;

    public MainViewModel(@NonNull Application application) {
        super(application);

        // Retrieve the alarms list from the db into a LiveData object
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the alarms from the DataBase");
        alarms = database.alarmDao().loadAllAlarms();

        // Start observing the LiveData in order to create geofence objects according to changes
        observer = new Observer<List<AlarmEntry>>() {
            @Override
            public void onChanged(@Nullable List<AlarmEntry> alarmEntries) {
                Log.d(TAG, "Creating and destroying geofences according to alarms list");
                List<Geofence> geofenceList = new ArrayList<>();
                if (alarmEntries != null) {
                    for(AlarmEntry entry : alarmEntries)
                    {
                        if (entry.isEnabled()) {
                            geofenceList.add(new Geofence.Builder()
                                    // Set the request ID of the geofence.
                                    // This is a string to identify this geofence.
                                    .setRequestId(String.valueOf(entry.getId()))

                                    .setCircularRegion(
                                            entry.getLatitude(),
                                            entry.getLongitude(),
                                            entry.getRadius()
                                    )
                                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                                    .build());
                        }
                    }
                }
            }
        };
        alarms.observeForever(observer);
    }

    public LiveData<List<AlarmEntry>> getAlarms() {
        return alarms;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        // Clear subscription to live data
        alarms.removeObserver(observer);
    }
}
