package com.gusta.wakemehome.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.gusta.wakemehome.DetailActivity;
import com.gusta.wakemehome.database.AppDatabase;
import com.gusta.wakemehome.utilities.Constants;
import com.gusta.wakemehome.utilities.NotificationUtils;

/**
 * JobIntentService class to handle notification action buttons clicks.
 */
public class NotificationActionsJobIntentService extends JobIntentService {

    // Constant for logging
    private static final String TAG = NotificationActionsJobIntentService.class.getSimpleName();

    // TODO: Refactor use of constants
    private static final int JOB_ID = 571;

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
        String action = intent.getAction();
        Log.d(TAG, "action is: " + action);
        assert action != null;

        if (action.equals(Constants.ACTION_DISMISS_ALARM)) {
            //TODO change DetailActivity.EXTRA_ALARM_ID & DetailActivity.DEFAULT_ALARM_ID to constants
            int id = intent.getIntExtra(DetailActivity.EXTRA_ALARM_ID, DetailActivity.DEFAULT_ALARM_ID);
            AppDatabase database = AppDatabase.getInstance(this.getApplication());
            database.alarmDao().updateAlarmEnabled(id, false);
            Intent stopIntent = new Intent(this, RingtonePlayingService.class);
            this.stopService(stopIntent);
        } else if (action.equals(Constants.ACTION_OPEN_SETTINGS)) {
            Intent settingsIntent =
                    new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        NotificationUtils.cancelNotification(this);
    }
}
