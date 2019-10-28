package com.gusta.wakemehome.maps;

import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

abstract class MapProvider {

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    protected static final LatLng DEFAULT_LOCATION = new LatLng(-33.8523341, 151.2106085);
    protected static final int DEFAULT_ZOOM = 15;

    //first usage of maps
    protected static boolean isFirstUsage = false;

    protected MapsActivity mMapsActivity;
    protected MapDestination mMapDestination;
    protected Geocoder mGeocoder;

    MapProvider(MapsActivity mapsActivity) {
        mMapsActivity = mapsActivity;
        mGeocoder = new Geocoder(mMapsActivity, Locale.getDefault());
        isFirstUsage = true;
    }

    abstract void updateRadius(float radius);

    MapDestination getMapDestination() {
        if (mMapDestination == null || !mMapDestination.isValidAddress()) {
            return null;
        }
        return mMapDestination;
    }

    void setMapDestination(MapDestination mMapDestination) {
        this.mMapDestination = mMapDestination;
    }

}
