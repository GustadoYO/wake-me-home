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
    private String locationImgUri;

    public MapAddress(LatLng coordinates, float radius, Geocoder geocoder) {
        this.coordinates = coordinates;
        if(coordinates != null)
            this.location = getAddress(geocoder,coordinates.latitude,coordinates.longitude);
        this.radius = radius;
    }
    public MapAddress(String location, float radius, Geocoder geocoder) {
        this.location = location;
        if(location != null)
            this.coordinates = getCoordinatesAddress(geocoder,location);
    }
    public MapAddress(double latitude,double longitude, String location, float radius) {
        this.coordinates = new LatLng(latitude,longitude);
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
    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getLocationImgUri() {
        return locationImgUri;
    }

    public void setLocationImgUri(String locationImgUri) {
        this.locationImgUri = locationImgUri;
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

    public boolean isValidEntry(){
        return !( getCoordinates() == null || getLocation().isEmpty() || getRadius() == 0 );
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

    private MapAddress(Parcel in) {
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        coordinates = new LatLng(latitude,longitude);
        radius = in.readFloat();
        location = in.readString();
        locationImgUri = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeDouble(coordinates.latitude);
        out.writeDouble(coordinates.longitude);
        out.writeFloat(radius);
        out.writeString(location);
        out.writeString(locationImgUri);
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
