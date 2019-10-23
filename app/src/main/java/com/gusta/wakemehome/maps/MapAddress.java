package com.gusta.wakemehome.maps;

import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class MapAddress implements Parcelable {
    private LatLng coordinates;
    private float radius;
    private String location;

    public MapAddress(LatLng coordinates, float radius, Geocoder geocoder) {
        this.coordinates = coordinates;
        if(coordinates != null)
            this.location = getAddress(geocoder,coordinates.latitude,coordinates.longitude);
        this.radius = radius;
    }
    public MapAddress(String location, float radius, Geocoder geocoder) {
        this.location = location;
        this.radius = radius;
        if(location != null)
            this.coordinates = getCoordinatesAddress(geocoder,location);
    }
    public MapAddress(double latitude,double longitude, String location, float radius) {
        this.coordinates = new LatLng(latitude, longitude);
        this.location = location;
        this.radius = radius;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public void setCoordinates(LatLng coordinates, Geocoder geocoder) {
        this.coordinates = coordinates;
        if(coordinates != null)
            this.location = getAddress(geocoder,coordinates.latitude,coordinates.longitude);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
    public double getLatitude() {
        return coordinates.latitude;
    }
    public double getLongitude() {
        return coordinates.longitude;
    }
    public String getLocation() {
        return location;
    }
    public float getRadius() {
        return radius;
    }

    @Override
    public String toString() {
        return location + " " + coordinates.toString();
    }

    boolean isValidEntry(){
        return !( getCoordinates() == null || getLocation() == null || getLocation().isEmpty() || getRadius() < 0 );
    }

    static String getAddress(Geocoder geocoder, double lat, double lng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String location = obj.getAddressLine(0);

            return location;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static LatLng getCoordinatesAddress(Geocoder geocoder, String address) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            Address obj = addresses.get(0);

            LatLng coordinates = new LatLng(obj.getLatitude(),obj.getLongitude());
            return coordinates;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /////////////////////////////
    // Parcelable METHODS ///////
    /////////////////////////////

    private MapAddress(Parcel in) {
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        coordinates = new LatLng(latitude,longitude);
        radius = in.readFloat();
        location = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(coordinates.latitude);
        out.writeDouble(coordinates.longitude);
        out.writeFloat(radius);
        out.writeString(location);
    }

    public static final Parcelable.Creator<MapAddress> CREATOR = new Parcelable.Creator<MapAddress>() {
        public MapAddress createFromParcel(Parcel in) {
            return new MapAddress(in);
        }

        public MapAddress[] newArray(int size) {
            return new MapAddress[size];
        }
    };

}
