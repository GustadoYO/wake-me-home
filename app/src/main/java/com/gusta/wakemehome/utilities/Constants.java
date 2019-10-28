package com.gusta.wakemehome.utilities;

public final class Constants {

    private Constants() {
    }

    private static final String PACKAGE_NAME = "com.gusta.wakemehome";

    // Actions
    public static final String ACTION_GEOFENCE_TRANSITION_OCCURRED =
            PACKAGE_NAME + ".ACTION_GEOFENCE_TRANSITION_OCCURRED";
    public static final String ACTION_DISMISS_ALARM = PACKAGE_NAME + ".ACTION_DISMISS_ALARM";
    public static final String ACTION_OPEN_SETTINGS = PACKAGE_NAME + ".ACTION_OPEN_SETTINGS";

    // Extras
    public static final String EXTRA_ALARM_ID = "extraAlarmId"; // MainActivity to DetailActivity
    public static final String EXTRA_ALARM_DESTINATION = "extraAlarmDestination"; // MapsActivity to DetailActivity
    public static final String EXTRA_RINGTONE_URI = "extraRingtoneUri"; // GeofenceTransitionsJobIntentService to RingtonePlayingService
    public static final String EXTRA_SHOULD_VIBRATE = "extraShouldVibrate"; // GeofenceTransitionsJobIntentService to RingtonePlayingService

    // Default values
    public static final int DEFAULT_ALARM_ID = -1;

    // Channels
    public static final String CHANNEL_ID = "channel_01";

    // Request codes
    public static final int ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE = 34;

    // Paths
    public static final String MAPS_DIR = "mapsDir";
    public static final String TEMP_IMAGE_FILE_NAME = "temp.png";
}
