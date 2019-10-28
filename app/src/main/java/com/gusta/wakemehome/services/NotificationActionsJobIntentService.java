package com.gusta.wakemehome.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.gusta.wakemehome.DetailActivity;
import com.gusta.wakemehome.database.AppDatabase;
import com.gusta.wakemehome.utilities.NotificationUtils;

/**
 * JobIntentService class to handle notification action buttons clicks.
 */
public class NotificationActionsJobIntentService extends JobIntentService {

    // Constant for logging
    private static final String TAG = NotificationActionsJobIntentService.class.getSimpleName();

    // TODO: Refactor use of constants
    private static final int JOB_ID = 571;

    private AppDatabase database;

    public NotificationActionsJobIntentService(){
        // Retrieve the alarms list from the db into a LiveData object
        database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the alarms from the DataBase");
    }
    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, NotificationActionsJobIntentService.class, JOB_ID, intent);
    }

    /**
     * Handles incoming intents.
     *
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //TODO change DetailActivity.EXTRA_ALARM_ID & DetailActivity.DEFAULT_ALARM_ID to constants
        int id = intent.getIntExtra(DetailActivity.EXTRA_ALARM_ID, DetailActivity.DEFAULT_ALARM_ID);
        database.alarmDao().updateAlarmEnabled(id, false);
        Intent stopIntent = new Intent(this, RingtonePlayingService.class);
        this.stopService(stopIntent);
        NotificationUtils.cancelNotification(this);
    }

}
