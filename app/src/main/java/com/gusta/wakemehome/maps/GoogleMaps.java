package com.gusta.wakemehome.maps;

import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import android.graphics.Bitmap;
import android.location.Location;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.gusta.wakemehome.R;

import java.util.Arrays;

import static com.gusta.wakemehome.maps.MapAddress.getAddress;

//TODO refactor user permissions from global utils
public class GoogleMaps extends MapProvider implements OnMapReadyCallback{

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int CIRCLE_WIDTH = 6;
    private static final int CIRCLE_DIVIDOR_SCALE = 600;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private GoogleMap mMap;
    private Circle mMapRadiusCircle;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    public GoogleMaps(MapsActivity mapsActivity){
        super(mapsActivity);

        SupportMapFragment mapFragment = (SupportMapFragment) mMapsActivity.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize Places.
        Places.initialize(mMapsActivity.getApplicationContext(), mMapsActivity.getString(R.string.google_maps_key));
        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(mMapsActivity);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mMapsActivity);


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                mMapsActivity.getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                LatLng cord = getAddress(mGeocoder, place.getName());
                setMarker(cord);
                Log.d(TAG, "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     * In this case we'll provide option to select coordinates by click on map
     * By default it'll take the current position and in case of existing
     * alarm it'll select the existing position
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Setting a click event handler for the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng coordinates) {

                setMarker(coordinates);
                updateRadius(mMapAddress.getRadius());

            }
        });

        if(isDefaultAddress()) {

            // Prompt the user for permission.
            getLocationPermission();

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();

            // Get the current location of the device and set the position of the map.
            getDeviceLocation();

        }else{

            setMarker(mMapAddress.getCoordinates());
            updateRadius(mMapAddress.getRadius());

        }

    }

    private void setMarker(LatLng coordinate){
        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        markerOptions.position(coordinate);

        mMapAddress.setCoordinates(coordinate,mGeocoder);
        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(mMapAddress.getLocation());

        // Clears the previously touched position
        mMap.clear();

        // Animating to the touched position
        mMap.animateCamera(CameraUpdateFactory.newLatLng(coordinate));

        // Placing a marker on the touched position
        mMap.addMarker(markerOptions).showInfoWindow();
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(mMapsActivity, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        LatLng coordinates;
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            coordinates = new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    coordinates , DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            coordinates = DEFAULT_LOCATION;
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(DEFAULT_LOCATION, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                        setMarker(coordinates);
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(mMapsActivity.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(mMapsActivity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    void updateRadius(float radius){

        //remove the old circle
        if(mMapRadiusCircle != null) {
            mMapRadiusCircle.remove();
        }

        LatLng coordinate = mMapAddress.getCoordinates();

        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();

        // Specifying the center of the circle
        circleOptions.center(coordinate);

        // Radius of the circle
        circleOptions.radius(radius);

        // Border color of the circle
        circleOptions.strokeColor(R.color.colorPrimary)
                         .fillColor(R.color.colorPrimaryLight);

        // Border width of the circle
        circleOptions.strokeWidth(CIRCLE_WIDTH);

        // Adding the circle to the GoogleMap
        mMapRadiusCircle = mMap.addCircle(circleOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                circleOptions.getCenter(), getZoomLevel(radius)));

        CaptureMapScreen();
    }

    /**
     * update zoon level calculate the different from default zoom level and radius selection for
     * zoom out in selection of radius
     * @param radius
     * @return
     */
    private int getZoomLevel(float radius) {
        int zoomLevel = DEFAULT_ZOOM;
        if (radius > 0) {
            double scale = radius / CIRCLE_DIVIDOR_SCALE;
            zoomLevel = (int) (DEFAULT_ZOOM - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }

    private void CaptureMapScreen()
    {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                //save to internal storage as temp.png and in case of alarm saving
                //it'll change to alarm id.png so it'll be max 1 temp map snapshot file
                saveToInternalStorage(snapshot);
            }
        };

        mMap.snapshot(callback);
    }

}
