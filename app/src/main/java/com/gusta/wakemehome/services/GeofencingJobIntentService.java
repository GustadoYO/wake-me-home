package com.gusta.wakemehome.services;

import android.util.Log;

import androidx.core.app.JobIntentService;
import androidx.lifecycle.LiveData;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

import java.util.List;

abstract class GeofencingJobIntentService extends JobIntentService {

    // Constant for logging
    private static final String TAG = GeofencingJobIntentService.class.getSimpleName();

    protected LiveData<List<AlarmEntry>> mAlarms;

    /**
     * Override getter to get specific class tag.
     *
     * @return Class tag.
     */
    abstract protected String getTag();

    /**
     * Initialize alarms list.
     */
    public GeofencingJobIntentService() {
        // Retrieve the alarms list from the db into a LiveData object
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the alarms from the DataBase");
        mAlarms = database.alarmDao().loadAllAlarms();
    }
}
