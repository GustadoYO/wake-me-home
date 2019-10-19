package com.gusta.wakemehome.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.gusta.wakemehome.geofencing.GeofenceEntry;

@Entity(tableName = "alarm")
public class AlarmEntry implements GeofenceEntry {

    public static final int DEFAULT_ALARM_ID = -1;

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
//    private String imageUri;       // Audio alert to play when alarm triggers

    @Ignore
    public AlarmEntry() {
        id = DEFAULT_ALARM_ID;
        enabled = true;
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
//        this.imageUri = imageUri;
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
//        this.imageUri = imageUri;
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

//    public String getImageUri() {
//        return imageUri;
//    }
//
//    public void setImageUri(String imageUri) {
//        this.imageUri = imageUri;
//    }
}
