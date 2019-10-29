package com.gusta.wakemehome.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.JobIntentService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

import java.util.List;

abstract class GeofencingJobIntentService extends JobIntentService {

    // Constant for logging
    private static final String TAG = GeofencingJobIntentService.class.getSimpleName();

    protected LiveData<List<AlarmEntry>> mAlarms;
    protected Observer<List<AlarmEntry>> mObserver;

    /**
     * Override getter to get specific class tag.
     *
     * @return Class tag.
     */
    abstract protected String getTag();

    /**
     * Start observing the alarm list.
     */
    protected void loadAlarms() {
        // Initialise the observer with the function that will be implemented in the child class
        mObserver = new Observer<List<AlarmEntry>>() {
            @Override
            public void onChanged(List<AlarmEntry> alarmEntries) {
                onAlarmsLoaded();
            }
        };

        // Retrieve the alarms list from the db into a LiveData object
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the alarms from the DataBase");
        mAlarms = database.alarmDao().loadAllAlarms();

        // 'observeForever(..)' must be called from the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mAlarms.observeForever(mObserver);
            }
        });
    }

    /**
     * Override method to implement the logic to run after alarms load.
     */
    abstract void onAlarmsLoaded();

    /**
     * Clear observer.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAlarms != null && mObserver != null) mAlarms.removeObserver(mObserver);
    }
}
