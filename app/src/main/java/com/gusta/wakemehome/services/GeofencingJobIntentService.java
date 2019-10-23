package com.gusta.wakemehome.services;

import android.util.Log;

import androidx.core.app.JobIntentService;
import androidx.lifecycle.LiveData;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

import java.util.List;

abstract class GeofencingJobIntentService extends JobIntentService {

    /**
     * Override getter to get specific class tag.
     *
     * @return Class tag.
     */
    abstract protected String getTag();

    /**
     * Get all alarms from the databse.
     *
     * @return Alarms list inside a LiveData object.
     */
    protected LiveData<List<AlarmEntry>> getAlarmsFromDb() {
        // Retrieve the alarms list from the db
        AppDatabase database = AppDatabase.getInstance(getApplication());
        Log.d(getTag(), "Actively retrieving the alarms from the DataBase");
        return database.alarmDao().loadAllAlarms();
    }
}
