package com.gusta.wakemehome.database;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gusta.wakemehome.geofencing.GeofenceManager;

@Entity(tableName = "alarm")
public class AlarmEntry implements GeofenceManager.GeofenceEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String location;    // Human readable location string
    private double latitude;    // The location's latitude
    private double longitude;   // The location's longitude
    private float radius;      // Radius from location to start alarm
    @ColumnInfo(name = "is_enabled")
    private boolean enabled;    // True if alarm is active
    @ColumnInfo(name = "should_vibrate")
    private boolean vibrate;    // True if alarm should vibrate
    private String message;     // Message to show when alarm triggers
    private String alert;       // Audio alert to play when alarm triggers

    @Ignore
    public AlarmEntry(int id) {
        this.id = id;
    }

    @Ignore
    public AlarmEntry(String location, double latitude, double longitude, float radius,
                      boolean enabled, boolean vibrate, String message, String alert) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.enabled = enabled;
        this.vibrate = vibrate;
        this.message = message;
        this.alert = alert;
    }

    public AlarmEntry(int id, String location, double latitude, double longitude, float radius,
                      boolean enabled, boolean vibrate, String message, String alert) {
        this.id = id;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.enabled = enabled;
        this.vibrate = vibrate;
        this.message = message;
        this.alert = alert;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        AlarmEntry alarm = (AlarmEntry) obj;
        return id == alarm.id;
    }
}
