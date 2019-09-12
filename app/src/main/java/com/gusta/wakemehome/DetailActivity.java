package com.gusta.wakemehome;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private String mAlarm;
    private TextView mAlarmDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mAlarmDisplay = (TextView) findViewById(R.id.tv_display_alarm);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                mAlarm = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
                mAlarmDisplay.setText(mAlarm);
            }
        }
    }

    /**
     * This method uses the URI scheme for showing the alarm on a
     * map. This super-handy intent is detailed in the "Common Intents"
     * page of Android's developer site:
     *
     * @see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
     *
     * Hint: Hold Command on Mac or Control on Windows and click that link
     * to automagically open the Common Intents page
     *
     * @return The Intent to use to open the map.
     */
    private Intent createOpenAlarmInMapIntent() {
        Uri geoLocation = Uri.parse("geo:0,0?q=" + mAlarm);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_map);
        menuItem.setIntent(createOpenAlarmInMapIntent());
        return true;
    }
}
