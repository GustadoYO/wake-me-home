package com.gusta.wakemehome.data;

import android.provider.BaseColumns;

/**
 * Defines table and column names for the alarms database. This class is not necessary, but keeps
 * the code organized.
 */
public class AlarmsContract {

    /* Inner class that defines the table contents of the alarms table */
    public static final class AlarmEntry implements BaseColumns {

        /* Used internally as the name of our alarms table. */
        public static final String TABLE_NAME = "alarms";

        /* Human readable location string */
        public static final String COLUMN_LOCATION = "location";

        /* The latitude and longitude, used to uniquely pinpoint the location on the map */
        public static final String COLUMN_COORD_LAT = "coord_lat";
        public static final String COLUMN_COORD_LONG = "coord_long";

        /* True if alarm is active */
        public static final String COLUMN_ENABLED = "enabled";

        /* True if alarm should vibrate */
        public static final String COLUMN_VIBRATE = "vibrate";

        /* Message to show when alarm triggers */
        public static final String COLUMN_MESSAGE = "message";

        /* Audio alert to play when alarm triggers */
        public static final String COLUMN_ALERT = "alert";

    }
}
