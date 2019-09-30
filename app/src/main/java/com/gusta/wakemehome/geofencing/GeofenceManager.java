package com.gusta.wakemehome.geofencing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

public class GeofenceManager {

    // Constant for logging
    private static final String TAG = GeofenceManager.class.getSimpleName();

    private Activity mActivity;
    private LiveData<? extends List<? extends GeofenceEntry>> mLiveData;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;

    public GeofenceManager(Activity activity, LiveData<? extends List<? extends GeofenceEntry>> liveData) {
        mActivity = activity;
        mLiveData = liveData;
        mGeofencingClient = LocationServices.getGeofencingClient(activity);
    }

    private GeofencingRequest getGeofencingRequest() {
        Log.d(TAG, "Creating and destroying geofences according to alarms list");
        List<? extends GeofenceEntry> entries = mLiveData.getValue();
        List<Geofence> geofenceList = new ArrayList<>();

        // Create geofence objects
        if (entries != null) {
            for(GeofenceEntry entry : entries)
            {
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

        // Specify geofences and initial triggers
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mActivity, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(mActivity, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    public void addGeofences() {
        // TODO: Ask for permissions (android.permission.ACCESS_FINE_LOCATION)
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(mActivity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Geofences added");
                    }
                })
                .addOnFailureListener(mActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to add geofences");
                    }
                });
    }

    public void removeGeofences() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(mActivity, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Geofences removed");
                    }
                })
                .addOnFailureListener(mActivity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to remove geofences");
                    }
                });
    }
}
