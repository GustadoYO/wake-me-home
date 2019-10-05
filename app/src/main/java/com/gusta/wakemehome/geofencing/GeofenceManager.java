package com.gusta.wakemehome.geofencing;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.gusta.wakemehome.BuildConfig;
import com.gusta.wakemehome.R;
import com.gusta.wakemehome.utilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class GeofenceManager {

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
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE, UPDATE
    }

    //=========//
    // MEMBERS //
    //=========//

    /**
     * The activity to show messages (like errors) on.
     */
    private ContextWrapper mContextWrapper;
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

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;

    //================//
    // PUBLIC METHODS //
    //================//

    public GeofenceManager(ContextWrapper activity, LiveData<? extends List<? extends GeofenceEntry>> liveData) {
        mContextWrapper = activity;
        mLiveData = liveData;
        mGeofencingClient = LocationServices.getGeofencingClient(activity);
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofences() {
        if (missingPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            requestPermissions();
            return;
        }
        addGeofencesTask();
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofences() {
        if (missingPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            requestPermissions();
            return;
        }
        removeGeofencesTask();
    }

    /**
     * Update geofences client according to current list.
     */
    public void updateGeofences() {
        if (missingPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.UPDATE;
            requestPermissions();
            return;
        }
        updateGeofencesTask();
    }

    //=================//
    // PRIVATE METHODS //
    //=================//

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
        Intent intent = new Intent(mContextWrapper, GeofenceBroadcastReceiver.class);
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
        if (missingPermissions()) {
            showSnackbar(mContextWrapper.getString(R.string.insufficient_permissions));
            return;
        }

        // Get the geofencing request. If it's null, there are no enabled geofences in the list
        GeofencingRequest geofencingRequest = getGeofencingRequest();
        if (geofencingRequest == null) {
            Log.d(TAG, "No geofences to add");
            return;
        }

        // If the context wrapper is an activity - errors can be shown on it
        if (mContextWrapper instanceof Activity) {
            Activity activity = (Activity) mContextWrapper;
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnSuccessListener(activity, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Geofences added");
                        }
                    })
                    .addOnFailureListener(activity, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //TODO: Handle StatusCode = 1000: "Geofence service is not available now.
                            // Typically this is because the user turned off location access in
                            // settings > location access."
                            Log.d(TAG, "Failed to add geofences");
                        }
                    });

            // If the context wrapper is not an activity, a notification is needed to show errors
        } else {
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
                        }
                    });
        }
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    private void removeGeofencesTask() {
        if (missingPermissions()) {
            showSnackbar(mContextWrapper.getString(R.string.insufficient_permissions));
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
                    }
                });
    }

    /**
     * Updates geofences. This method should be called after the user has granted the location
     * permission.
     */
    private void updateGeofencesTask() {
        if (missingPermissions()) {
            showSnackbar(mContextWrapper.getString(R.string.insufficient_permissions));
            return;
        }

        // TODO: Add & remove only deltas from current geofences
        removeGeofencesTask();
        addGeofencesTask();
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        // Abort if the context wrapper is not an activity (can't show messages to the user)
        if (!(mContextWrapper instanceof Activity)) return;
        Activity activity = (Activity) mContextWrapper;

        // Show the message to the user
        View container = activity.findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        // Abort if the context wrapper is not an activity (can't show messages to the user)
        if (!(mContextWrapper instanceof Activity)) return;
        Activity activity = (Activity) mContextWrapper;

        // Show the message to the user
        Snackbar.make(
                activity.findViewById(android.R.id.content),
                mContextWrapper.getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(mContextWrapper.getString(actionStringId), listener).show();
    }

    /**
     * Ask the user for permission.
     */
    private void requestPermissions() {
        // Abort if the context wrapper is not an activity (can't show messages to the user)
        if (!(mContextWrapper instanceof Activity)) return;

        // Request permissions from the user
        final Activity activity = (Activity) mContextWrapper;
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale,
                    android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(activity,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    Constants.ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private void performPendingGeofenceTask() {
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
    }

    // TODO: Export permissions logic to a utility class
    /**
     * Return the current state of the permissions needed.
     */
    private boolean missingPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(mContextWrapper,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Callback received when a permissions request has been completed. Redirected from activity.
     */
    public void onRequestPermissionsResult(@NonNull int[] grantResults) {
        if (grantResults.length <= 0) {
            // If user interaction was interrupted, the permission request is cancelled and you
            // receive empty arrays.
            Log.i(TAG, "User interaction was cancelled.");
        } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted.");
            performPendingGeofenceTask();
        } else {
            // Permission denied.

            // Notify the user via a SnackBar that they have rejected a core permission for the
            // app, which makes the Activity useless. In a real app, core permissions would
            // typically be best requested during a welcome-screen flow.

            // Additionally, it is important to remember that a permission might have been
            // rejected without asking the user for permission (device policy or "Never ask
            // again" prompts). Therefore, a user interface affordance is typically implemented
            // when permissions are denied. Otherwise, your app could appear unresponsive to
            // touches or interactions which have required permissions.
            showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Build intent that displays the App settings screen.
                            Intent intent = new Intent();
                            intent.setAction(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    BuildConfig.APPLICATION_ID, null);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContextWrapper.startActivity(intent);
                        }
                    });
            mPendingGeofenceTask = PendingGeofenceTask.NONE;
        }
    }
}
