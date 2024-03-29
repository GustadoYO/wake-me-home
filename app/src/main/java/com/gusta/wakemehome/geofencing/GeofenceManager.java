package com.gusta.wakemehome.geofencing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.gusta.wakemehome.R;
import com.gusta.wakemehome.utilities.Constants;
import com.gusta.wakemehome.utilities.NotificationUtils;
import com.gusta.wakemehome.utilities.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class GeofenceManager implements PermissionUtils.PendingTaskHandler {

    //===========//
    // CONSTANTS //
    //===========//

    // Constant for logging
    private static final String TAG = GeofenceManager.class.getSimpleName();

    //=======//
    // ENUMS //
    //=======//

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum GeofenceTask {
        ADD, REMOVE, NONE, UPDATE
    }

    //============//
    // INTERFACES //
    //============//

    /**
     * An interface that an entry needs to implement in order to allow the GeofenceManager to
     * register geofences based on this entry.
     */
    public interface GeofenceEntry {
        int getId();

        double getLatitude();

        double getLongitude();

        float getRadius();

        boolean isEnabled();
    }

    //=========//
    // MEMBERS //
    //=========//

    /**
     * The activity to show messages (like errors) on.
     */
    private ContextWrapper mContextWrapper;
    /**
     * The broadcast receiver that will listen to the transition events.
     */
    private Class<? extends BroadcastReceiver> mBroadcastReceiverClass;
    /**
     * The list of entries tracked - each entry will need a geofence.
     */
    private LiveData<? extends List<? extends GeofenceEntry>> mLiveData;
    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;
    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;
    /**
     * The task waiting to be done (waiting for permissions)
     */
    private GeofenceTask mPendingGeofenceTask = GeofenceTask.NONE;

    //================//
    // PUBLIC METHODS //
    //================//

    public GeofenceManager(ContextWrapper contextWrapper,
                           Class<? extends BroadcastReceiver> broadcastReceiverClass,
                           LiveData<? extends List<? extends GeofenceEntry>> liveData) {
        mContextWrapper = contextWrapper;
        mBroadcastReceiverClass = broadcastReceiverClass;
        mLiveData = liveData;
        mGeofencingClient = LocationServices.getGeofencingClient(contextWrapper);
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofences() {
        modifyGeofences(GeofenceTask.ADD);
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofences() {
        modifyGeofences(GeofenceTask.REMOVE);
    }

    /**
     * Update geofences client according to current list.
     */
    public void updateGeofences() {
        modifyGeofences(GeofenceTask.UPDATE);
    }

    //=================//
    // PRIVATE METHODS //
    //=================//

    /**
     * Perform a wanted modify to the geofences list.
     *
     * @param geofenceTask The wanted modify task.
     */
    private void modifyGeofences(GeofenceTask geofenceTask) {

        // Remember task to perform
        mPendingGeofenceTask = geofenceTask;

        // Check permission and request if missing
        if (PermissionUtils.missingLocationPermissions(mContextWrapper)) {
            // TODO: Handle non-activity case
            if (mContextWrapper instanceof Activity)
                PermissionUtils.requestLocationPermissions((Activity) mContextWrapper);
            return;
        }

        // Perform task (if permission was not missing)
        performPendingTask();
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        Log.d(TAG, "Creating and destroying geofences according to alarms list");
        List<? extends GeofenceEntry> entries = mLiveData.getValue();
        List<Geofence> geofenceList = new ArrayList<>();
        GeofencingRequest geofencingRequest = null;

        // Create geofence objects
        if (entries != null) {
            for (GeofenceEntry entry : entries) {
                if (entry.isEnabled()) {
                    geofenceList.add(new Geofence.Builder()
                            // Set the request ID of the geofence. This is a string to identify this
                            // geofence.
                            .setRequestId(String.valueOf(entry.getId()))
                            .setCircularRegion(
                                    entry.getLatitude(),
                                    entry.getLongitude(),
                                    entry.getRadius()
                            )
                            .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                            .build());
                }
            }
        }

        // Specify geofences and initial triggers (if any geofences needed)
        if (!geofenceList.isEmpty()) {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(geofenceList);
            geofencingRequest = builder.build();
        }

        return geofencingRequest;
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContextWrapper, mBroadcastReceiverClass);
        intent.setAction(Constants.ACTION_GEOFENCE_TRANSITION_OCCURRED);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContextWrapper, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    private void addGeofencesTask() {
        if (PermissionUtils.missingLocationPermissions(mContextWrapper)) {
            NotificationUtils.notifyUser(mContextWrapper,
                        mContextWrapper.getString(R.string.insufficient_permissions));
            return;
        }

        // Get the geofencing request. If it's null, there are no enabled geofences in the list
        GeofencingRequest geofencingRequest = getGeofencingRequest();
        if (geofencingRequest == null) {
            Log.d(TAG, "No geofences to add");
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Geofences added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO: Handle StatusCode = 1000: "Geofence service is not available now.
                        // Typically this is because the user turned off location access in
                        // settings > location access."
                        Log.d(TAG, "Failed to add geofences");
                        handleError(e);
                    }
                });
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private void removeGeofencesTask() {
        if (PermissionUtils.missingLocationPermissions(mContextWrapper)) {
            NotificationUtils.notifyUser(mContextWrapper,
                    mContextWrapper.getString(R.string.insufficient_permissions));
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Geofences removed");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to remove geofences");
                        handleError(e);
                    }
                });
    }

    /**
     * Updates geofences. This method should be called after the user has granted the location
     * permission.
     */
    private void updateGeofencesTask() {
        if (PermissionUtils.missingLocationPermissions(mContextWrapper)) {
            NotificationUtils.notifyUser(mContextWrapper,
                    mContextWrapper.getString(R.string.insufficient_permissions));
            return;
        }

        // TODO: Add & remove only deltas from current geofences
        removeGeofencesTask();
        addGeofencesTask();
    }

    /**
     * Handle geofences tasks failures.
     *
     * @param e The exception received.
     */
    private void handleError(Exception e) {
        // TODO: Use JobScheduler to re-register geofences once location access is turned on

        // Build intent that displays the App settings screen.
        final Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

        // Notify the user about the error
        NotificationUtils.notifyUser(mContextWrapper, R.string.geofence_not_available_title,
                R.string.geofence_not_available_text, R.string.settings, intent,
                false);
    }

    //============================//
    // PendingTaskHandler METHODS //
    //============================//

    /**
     * Performs the geofencing task that was pending until location permission was granted or
     * checked.
     */
    public void performPendingTask() {
        switch (mPendingGeofenceTask) {
            case ADD:
                addGeofencesTask();
                break;
            case REMOVE:
                removeGeofencesTask();
                break;
            case UPDATE:
                updateGeofencesTask();
                break;
        }
        mPendingGeofenceTask = GeofenceTask.NONE;
    }

}
