package com.gusta.wakemehome.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

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
    public static final int DEFAULT_ALARM_ID = -1;
    private static final double DEFAULT_VALUE_LATITUDE = 1000;
    private static final double DEFAULT_VALUE_LONGITUDE = 1000;

    @Ignore
    public AlarmEntry(){
        this.id = DEFAULT_ALARM_ID;
        this.latitude = DEFAULT_VALUE_LATITUDE;
        this.longitude = DEFAULT_VALUE_LONGITUDE;
    }

    @Ignore
    public AlarmEntry(String location, double latitude, double longitude, double radius,
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

    public AlarmEntry(int id, String location, double latitude, double longitude, double radius,
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

    public boolean isNewEntry(){
        return this.id == DEFAULT_ALARM_ID;
    }

    public boolean isValidEntry(){
        return !(this.latitude == DEFAULT_VALUE_LATITUDE || this.longitude == DEFAULT_VALUE_LONGITUDE || this.radius == 0);
    }
}
