package com.gusta.wakemehome.maps;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    MapProvider(MapsActivity mapsActivity){
        mMapsActivity = mapsActivity;
        mGeocoder = new Geocoder(mMapsActivity, Locale.getDefault());
        isFirstUsage = true;
    }

    abstract void updateRadius(float radius);

    MapAddress getMapAddress() {
        if(mMapAddress == null || !mMapAddress.isValidEntry()){
            return null;
        }
        return mMapAddress;
    }

    void setMapAddress(MapAddress mMapAddress) {
        this.mMapAddress = mMapAddress;
    }

    //TODO: Move it to utils
    void saveToInternalStorage(Bitmap bitmapImage){
        String dirPath = getLocalMapDir();
        // Create imageDir
        File path = new File(dirPath + "/" + com.gusta.wakemehome.DetailActivity.TEMP_IMAGE_FILE);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //TODO: Move it to utils
    private String getLocalMapDir(){
        ContextWrapper cw = new ContextWrapper(mMapsActivity);

        File directory = cw.getDir("mapsDir", Context.MODE_PRIVATE);
        return directory.getAbsolutePath();
    }
}
