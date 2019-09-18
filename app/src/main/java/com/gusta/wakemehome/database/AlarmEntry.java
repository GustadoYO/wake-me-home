package com.gusta.wakemehome.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "alarm")
public class AlarmEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String location;    // Human readable location string
    private double latitude;    // The location's latitude
    private double longitude;   // The location's longitude
    private double radius;      // Radius from location to start alarm
    @ColumnInfo(name = "is_enabled")
    private boolean enabled;    // True if alarm is active
    @ColumnInfo(name = "should_vibrate")
    private boolean vibrate;    // True if alarm should vibrate
    private String message;     // Message to show when alarm triggers
    private String alert;       // Audio alert to play when alarm triggers

    @Ignore
    public AlarmEntry(String location, double latitude, double longitude, double radius, boolean enabled, boolean vibrate, String message, String alert) {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.enabled = enabled;
        this.vibrate = vibrate;
        this.message = message;
        this.alert = alert;
    }

    public AlarmEntry(int id, String location, double latitude, double longitude, double radius, boolean enabled, boolean vibrate, String message, String alert) {
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

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
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
}
