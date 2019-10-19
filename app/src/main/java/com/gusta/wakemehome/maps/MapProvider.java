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

public abstract class MapProvider {
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    protected final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    protected static final int DEFAULT_ZOOM = 15;

    protected MapsActivity mMapsActivity;
    protected MapAddress mMapAddress;
    protected Geocoder mGeocoder;

    public MapProvider(MapsActivity mapsActivity){
        mMapsActivity = mapsActivity;
        mGeocoder = new Geocoder(mMapsActivity, Locale.getDefault());
        //default selection
        mMapAddress = new MapAddress(mDefaultLocation,0,mGeocoder);
    }
    public void updateSelectedLocation(float radius){}

    public MapAddress getMapAddress() {
        if(mMapAddress == null || !mMapAddress.isValidEntry()){
            return null;
        }
        return mMapAddress;
    }

    public void setMapAddress(MapAddress mMapAddress) {
        this.mMapAddress = mMapAddress;
    }

    protected boolean isDefaultAddress(){
        if(mMapAddress.getCoordinates() == mDefaultLocation){
            return true;
        }
        return false;
    }

    //TODO: Handle deleted alarms should be in separate process which will check the storage
    // every week/day.
    protected String saveToInternalStorage(Bitmap bitmapImage,String filename){
        File path;
        if(mMapAddress.getLocationImgUri() == null) {
            ContextWrapper cw = new ContextWrapper(mMapsActivity.getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("mapsDir", Context.MODE_PRIVATE);
            // Create imageDir
            path = new File(directory, filename);
        }else{
            path = new File(mMapAddress.getLocationImgUri());
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return path.getAbsolutePath();
    }

}
