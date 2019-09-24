package com.gusta.wakemehome.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import com.gusta.wakemehome.R;

final class WakeMeHomePreferences {

    /**
     * Returns true if the user has selected metric length display.
     *
     * @param context Context used to get the SharedPreferences
     *
     * @return true If metric display should be used
     */
    static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String keyForUnits = context.getString(R.string.pref_units_key);
        String defaultUnits = context.getString(R.string.pref_units_metric);
        String preferredUnits = prefs.getString(keyForUnits, defaultUnits);
        String metric = context.getString(R.string.pref_units_metric);
        boolean userPrefersMetric;
        userPrefersMetric = metric.equals(preferredUnits);
        return userPrefersMetric;
    }

}
