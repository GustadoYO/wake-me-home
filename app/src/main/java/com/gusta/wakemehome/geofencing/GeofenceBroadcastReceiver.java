package com.gusta.wakemehome.geofencing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import com.gusta.wakemehome.ReRegisterGeofencesJobIntentService;
import com.gusta.wakemehome.utilities.Constants;

/**
 * Receiver for geofence transition changes.
 * <p>
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a JobIntentService
 * that will handle the intent in the background.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    // Constant for logging
    private static final String TAG = BroadcastReceiver.class.getSimpleName();

    /**
     * Receives incoming intents.
     *
     * @param context the application context.
     * @param intent  sent by Location Services. This Intent is provided to Location
     *                Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "action is: " + action);

        // Enqueues a JobIntentService passing the context and intent as parameters
        assert action != null;
        if (action.equals(Constants.ACTION_GEOFENCE_TRANSITION_OCCURRED)) {
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent);
        }
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
            ReRegisterGeofencesJobIntentService.enqueueWork(context, intent);
        }
    }
}