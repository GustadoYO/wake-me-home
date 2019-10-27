package com.gusta.wakemehome.utilities;

public final class Constants {

    private Constants() {
    }

    private static final String PACKAGE_NAME = "com.gusta.wakemehome";

    // TODO: Refactor use of constants
    public static final String ACTION_GEOFENCE_TRANSITION_OCCURRED =
            PACKAGE_NAME + ".ACTION_GEOFENCE_TRANSITION_OCCURRED";
    public static final String ACTION_DISMISS_ALARM = PACKAGE_NAME + ".ACTION_DISMISS_ALARM";

    public static final String CHANNEL_ID = "channel_01";

    public static final int ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE = 34;

    //temp png will be for unsaved snapshots on save it'll change to  map id.png
    public static final String MAPS_DIR = "mapsDir";
    public static final String TEMP_IMAGE_FILE_NAME = "temp.png";
}
