package com.gusta.wakemehome.utilities;

import android.content.Context;

import com.gusta.wakemehome.R;

/**
 * Contains useful utilities , such as conversion between Kilometres and Miles.
 */
public final class WakeMeHomeUnitsUtils {

    private static final String LOG_TAG = WakeMeHomeUnitsUtils.class.getSimpleName();

    /**
     * This method will convert a length from Kilometres to Miles.
     *
     * @param lengthInMetres Length in metres (m)
     *
     * @return Length in feet (ft)
     */
    private static double metresToFeet(double lengthInMetres) {
        return lengthInMetres * 3.28084;
    }

    /**
     * Length data is stored in Kilometres by our app. Depending on the user's preference,
     * the app may need to display the length in Miles. This method will perform that
     * length conversion if necessary. It will also format the length so that no
     * decimal points show. Length will be formatted to the following form: "21 km"
     *
     * @param context     Android Context to access preferences and resources
     * @param length      Length in Kilometres (km)
     *
     * @return Formatted length String in the following form:
     * "21 km"
     */
    public static String formatLength(Context context, double length) {
        int temperatureFormatResourceId = R.string.format_length_metres;

        if (!WakeMeHomePreferences.isMetric(context)) {
            length = metresToFeet(length);
            temperatureFormatResourceId = R.string.format_length_feet;
        }

        /* For presentation, assume the user doesn't care about tenths of a degree. */
        return String.format(context.getString(temperatureFormatResourceId), length);
    }

}
