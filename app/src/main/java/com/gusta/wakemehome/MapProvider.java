package com.gusta.wakemehome;

import com.google.android.gms.maps.model.LatLng;

public abstract class MapProvider {
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    protected final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    protected static final int DEFAULT_ZOOM = 15;

    protected MapsActivity mMapsActivity;
    public MapProvider(MapsActivity mapsActivity){
        mMapsActivity = mapsActivity;
    }
    protected void drawCircle(LatLng coordinate,double radius){}
    protected void setMarker(LatLng coordinate){}

}
