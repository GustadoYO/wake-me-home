package com.gusta.wakemehome.services;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.gusta.wakemehome.MainActivity;
import com.gusta.wakemehome.R;
import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.geofencing.GeofenceErrorMessages;
import com.gusta.wakemehome.utilities.NotificationUtils;

import java.util.List;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsJobIntentService extends GeofencingJobIntentService {

    // Constant for logging
    private static final String TAG = GeofenceTransitionsJobIntentService.class.getSimpleName();

    // TODO: Refactor use of constants
    private static final int JOB_ID = 573;

    @Override
    protected String getTag() {
        return TAG;
    }

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsJobIntentService.class, JOB_ID, intent);
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            handleError(geofencingEvent);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Send a notification for each triggered geofence
            sendNotifications(geofenceTransition, triggeringGeofences);

        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    /**
     * Handle geofencing intents that have errors.
     *
     * @param geofencingEvent The geofencing intent.
     */
    private void handleError(GeofencingEvent geofencingEvent) {
        int errorCode = geofencingEvent.getErrorCode();
        String errorMessage = GeofenceErrorMessages.getErrorString(this, errorCode);
        Log.e(TAG, errorMessage);

        if (errorCode == GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE) {
            // TODO: Use JobScheduler to re-register geofences once location access is turned on

            // Geofence service is not available now. Typically this is because the user turned off
            // location access in settings > location access. Send error notification.
            NotificationUtils.notifyUser(this,
                    getString(R.string.geofence_not_available_title),
                    getString(R.string.geofence_not_available_text),
                    getString(R.string.settings),
                    new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            );
        }
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    /**
     * Send a notification for each triggered geofence.
     *
     * @param geofenceTransition  The geofencing transition that occurred
     * @param triggeringGeofences The list of geofences that triggered
     */
    private void sendNotifications(int geofenceTransition, List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Get all alarms from database
        List<AlarmEntry> alarms = getAlarmsFromDb().getValue();
        assert alarms != null;

        // We want a notification for every triggering geofence
        for (Geofence geofence : triggeringGeofences) {

            // The geofence id is the same as the matching alarm id - get the matching alarm
            AlarmEntry alarm =
                    alarms.get(alarms.indexOf(new AlarmEntry(
                            Integer.parseInt(geofence.getRequestId()))));

            // Send notification and log the transition details.
            String NotificationTitle = geofenceTransitionString + " " + alarm.getLocation();
            NotificationUtils.notifyUser(this, NotificationTitle, alarm.getMessage(),
                    notificationIntent);
            Log.i(TAG, NotificationTitle);
        }
    }
}
