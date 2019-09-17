package com.gusta.wakemehome.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gusta.wakemehome.data.AlarmsContract.AlarmEntry;
/**
 * Manages a local database for alarms data.
 */
public class AlarmsDbHelper extends SQLiteOpenHelper {

    /*
     * This is the name of our database. Database names should be descriptive and end with the
     * .db extension.
     */
    public static final String DATABASE_NAME = "alarms.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION = 2;

    public AlarmsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        /*
         * This String will contain a simple SQL statement that will create a table that will
         * hold our alarms data.
         */
        final String SQL_CREATE_ALARMS_TABLE =

                "CREATE TABLE " + AlarmEntry.TABLE_NAME + " (" +

                        /*
                         * AlarmEntry did not explicitly declare a column called "_ID". However,
                         * AlarmEntry implements the interface, "BaseColumns", which does have a field
                         * named "_ID". We use that here to designate our table's primary key.
                         */
                        AlarmEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        AlarmEntry.COLUMN_LOCATION   + " TEXT NOT NULL, "                              +
                        AlarmEntry.COLUMN_COORD_LAT  + " REAL NOT NULL, "                              +
                        AlarmEntry.COLUMN_COORD_LONG + " REAL NOT NULL, "                              +
                        AlarmEntry.COLUMN_ENABLED    + " INTEGER NOT NULL, "                           +
                        AlarmEntry.COLUMN_VIBRATE    + " INTEGER NOT NULL, "                           +
                        AlarmEntry.COLUMN_MESSAGE    + " TEXT NOT NULL, "                              +
                        AlarmEntry.COLUMN_ALERT      + " TEXT NOT NULL" + ");";

        /*
         * After we've spelled out our SQLite table creation statement above, we actually execute
         * that SQL with the execSQL method of our SQLite database object.
         */
        sqLiteDatabase.execSQL(SQL_CREATE_ALARMS_TABLE);
    }

    /**
     * This database's "Upgrade policy". Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AlarmEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
