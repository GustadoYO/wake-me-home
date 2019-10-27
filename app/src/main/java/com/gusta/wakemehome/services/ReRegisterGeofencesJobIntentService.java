package com.gusta.wakemehome.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.gusta.wakemehome.geofencing.GeofenceManager;

/**
 * Listener for events that the system cannot recover the geofences in.
 */
public class ReRegisterGeofencesJobIntentService extends GeofencingJobIntentService {

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

    @Override
    protected String getTag() {
        return null;
    }

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
            mGeofenceManager = new GeofenceManager(this, mAlarms);
        }

        // TODO: Observe LiveData object to add geofences
        mGeofenceManager.addGeofences();
    }
}
