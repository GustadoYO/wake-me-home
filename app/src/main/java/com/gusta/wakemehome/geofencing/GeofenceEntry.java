package com.gusta.wakemehome.geofencing;

public interface GeofenceEntry {
    int getId();
    double getLatitude();
    double getLongitude();
    float getRadius();
    boolean isEnabled();
}
