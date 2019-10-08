package com.gusta.wakemehome;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class MapAddress implements Parcelable {
    private LatLng coordinates;
    private String addressName;

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
        addressName = addressName;
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
