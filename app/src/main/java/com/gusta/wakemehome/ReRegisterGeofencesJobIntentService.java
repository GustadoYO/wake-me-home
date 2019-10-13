package com.gusta.wakemehome;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.lifecycle.LiveData;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;
import com.gusta.wakemehome.geofencing.GeofenceManager;

import java.util.List;

/**
 * Listener for events that the system cannot recover the geofences in.
 */
public class ReRegisterGeofencesJobIntentService extends JobIntentService {

    //===========//
    // CONSTANTS //
    //===========//

    // Constant for logging
    private static final String TAG = ReRegisterGeofencesJobIntentService.class.getSimpleName();

    // TODO: Refactor use of constants
    private static final int JOB_ID = 572;

    //=========//
    // MEMBERS //
    //=========//

    private GeofenceManager mGeofenceManager;   // The geofence manager

    //=========//
    // METHODS //
    //=========//

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, ReRegisterGeofencesJobIntentService.class, JOB_ID, intent);
    }

    /**
     * Handles incoming intents.
     *
     * @param intent sent by Geofence Broadcast Receiver. Asks to Re-register geofences.
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        // Init the geofence manager if needed
        if (mGeofenceManager == null) {
            // Retrieve the alarms list from the db into a LiveData object
            AppDatabase database = AppDatabase.getInstance(this.getApplication());
            Log.d(TAG, "Actively retrieving the alarms from the DataBase");
            LiveData<List<AlarmEntry>> alarms = database.alarmDao().loadAllAlarms();
            mGeofenceManager = new GeofenceManager(this, alarms);
        }

        mGeofenceManager.addGeofences();
    }
}
