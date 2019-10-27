package com.gusta.wakemehome.services;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.gusta.wakemehome.R;
import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.geofencing.GeofenceBroadcastReceiver;
import com.gusta.wakemehome.geofencing.GeofenceErrorMessages;
import com.gusta.wakemehome.utilities.Constants;
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

    private Observer<List<AlarmEntry>> mObserver;

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

            // Launch the closest alarm that triggered
            launchRelevantAlarm(triggeringGeofences);

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
                    new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                    false);
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
     * Get the closest alarm that triggered.
     *
     * @param triggeringGeofences The list of geofences that triggered
     */
    private void launchRelevantAlarm(final List<Geofence> triggeringGeofences) {

        // Alarms are in LiveData, so observer is needed in order to use them
        mObserver = new Observer<List<AlarmEntry>>() {
            @Override
            public void onChanged(List<AlarmEntry> alarmEntries) {
                mAlarms.removeObserver(this);

                // Find the closest alarm that triggered
                AlarmEntry relevantAlarm = null;
                for (Geofence geofence : triggeringGeofences) {

                    // The geofence id is the same as the matching alarm id - get the matching alarm
                    AlarmEntry currAlarm =
                            alarmEntries.get(alarmEntries.indexOf(new AlarmEntry(
                                    Integer.parseInt(geofence.getRequestId()))));

                    // Choose alarm based on radius
                    if (relevantAlarm == null || relevantAlarm.getRadius() > currAlarm.getRadius()) {
                        relevantAlarm = currAlarm;
                    }
                }

                assert relevantAlarm != null;
                launchAlarm(relevantAlarm);
            }
        };

        // 'observeForever(..)' must be called from the main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mAlarms.observeForever(mObserver);
            }
        });
    }

    private void launchAlarm(AlarmEntry alarm) {

        Log.i(TAG, "Launching alarm: " + alarm.getId());

        // Play the selected ringtone. We use a Service in order to allow dismissing the alarm
        Intent startIntent = new Intent(this, RingtonePlayingService.class);
        startIntent.putExtra(RingtonePlayingService.EXTRA_RINGTONE_URI, alarm.getAlert());
        startIntent.putExtra(RingtonePlayingService.EXTRA_SHOULD_VIBRATE, alarm.isVibrate());
        this.startService(startIntent);

        // Get the transition details as a string and add alarm's location to form title
        String geofenceTransitionString = getTransitionString(Geofence.GEOFENCE_TRANSITION_ENTER);
        String NotificationTitle = geofenceTransitionString + " " + alarm.getLocation();

        // Create the dismiss button intent
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        intent.setAction(Constants.ACTION_DISMISS_ALARM);

        // Send notification and log the transition details.
        NotificationUtils.notifyUser(this, NotificationTitle, alarm.getMessage(),
                getString(R.string.dismiss), intent, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAlarms.removeObserver(mObserver);
    }
}
