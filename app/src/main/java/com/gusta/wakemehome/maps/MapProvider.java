package com.gusta.wakemehome.maps;

import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

abstract class MapProvider {
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    static final LatLng DEFAULT_LOCATION = new LatLng(-33.8523341, 151.2106085);
    //first usage of maps
    static boolean isFirstUsage = false;
    static final int DEFAULT_ZOOM = 15;

    MapsActivity mMapsActivity;
    MapAddress mMapAddress;
    Geocoder mGeocoder;

    MapProvider(MapsActivity mapsActivity) {
        mMapsActivity = mapsActivity;
        mGeocoder = new Geocoder(mMapsActivity, Locale.getDefault());
        isFirstUsage = true;
    }

    abstract void updateRadius(float radius);

    MapAddress getMapAddress() {
        if (mMapAddress == null || !mMapAddress.isValidEntry()) {
            return null;
        }
        return mMapAddress;
    }

    void setMapAddress(MapAddress mMapAddress) {
        this.mMapAddress = mMapAddress;
    }

}
