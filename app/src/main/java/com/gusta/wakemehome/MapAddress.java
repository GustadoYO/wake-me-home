package com.gusta.wakemehome;

import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class MapAddress implements Parcelable {
    private LatLng coordinates;
    private double radius;
    private String location;

    public MapAddress(LatLng coordinates, Geocoder geocoder) {
        this.coordinates = coordinates;
        this.location = getAddress(geocoder,coordinates.latitude,coordinates.longitude);
    }
    public MapAddress(String location, Geocoder geocoder) {
        this.coordinates = getCoordinatesAddress(geocoder,location);
        this.location = location;
    }
    public MapAddress(LatLng coordinates, String location) {
        this.coordinates = coordinates;
        this.location = location;
    }
    private MapAddress(Parcel in) {
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        coordinates = new LatLng(latitude,longitude);
        radius = in.readDouble();
        location = in.readString();
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
    public void setRadius(double radius) {
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
    public double getRadius() {
        return radius;
    }

    public boolean isValidEntry(){
        return !( getCoordinates() == null || getLocation().isEmpty() || getRadius() == 0 );
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return location + " " + coordinates.toString();
    }

    public static String getAddress(Geocoder geocoder, double lat,double lng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String location = obj.getAddressLine(0);

            return location;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LatLng getCoordinatesAddress(Geocoder geocoder, String address) {
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            Address obj = addresses.get(0);

            LatLng coordinates = new LatLng(obj.getLatitude(),obj.getLongitude());
            return coordinates;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(coordinates.latitude);
        out.writeDouble(coordinates.longitude);
        out.writeDouble(radius);
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
