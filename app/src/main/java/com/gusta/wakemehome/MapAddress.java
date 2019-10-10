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
    private String addressName;

    public MapAddress(LatLng coordinates, Geocoder geocoder) {
        this.coordinates = coordinates;
        this.addressName = getAddress(geocoder,coordinates.latitude,coordinates.longitude);
    }
    public MapAddress(String addressName, Geocoder geocoder) {
        this.coordinates = getCoordinatesAddress(geocoder,addressName);
        this.addressName = addressName;
    }
    public MapAddress(LatLng coordinates, String addressName) {
        this.coordinates = coordinates;
        this.addressName = addressName;
    }
    private MapAddress(Parcel in) {
        double latitude = in.readDouble();
        double longitude = in.readDouble();
        coordinates = new LatLng(latitude,longitude);
        addressName = in.readString();
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }
    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }
    public LatLng getCoordinates() {
        return coordinates;
    }
    public double getlatitude() {
        return coordinates.latitude;
    }
    public double getlongitude() {
        return coordinates.longitude;
    }
    public String getAddressName() {
        return addressName;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return addressName + " " + coordinates.toString();
    }

    public static String getAddress(Geocoder geocoder, double lat,double lng) {
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String add = obj.getAddressLine(0);

            return add;
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
        out.writeString(addressName);
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
