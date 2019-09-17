package com.gusta.wakemehome;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    // Extra for the alarm ID to be received in the intent
    public static final String EXTRA_ALARM_ID = "extraAlarmId";
    // Extra for the alarm ID to be received after rotation
    public static final String INSTANCE_ALARM_ID = "instanceAlarmId";
    // Constant for default alarm id to be used when not in update mode
    private static final int DEFAULT_ALARM_ID = -1;
    // Constant for logging
    private static final String TAG = DetailActivity.class.getSimpleName();
    // Fields for views
    EditText mLocation;
    EditText mLatitude;
    EditText mLongitude;
    EditText mRadius;
    EditText mEnabled;
    EditText mVibrate;
    EditText mMessage;
    EditText mAlert;
    Button mButton;

    private int mAlarmId = DEFAULT_ALARM_ID;

    // Member variable for the Database
    private AppDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initViews();

        mDb = AppDatabase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ALARM_ID)) {
            mAlarmId = savedInstanceState.getInt(INSTANCE_ALARM_ID, DEFAULT_ALARM_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ALARM_ID)) {
            mButton.setText(R.string.update_button);
            if (mAlarmId == DEFAULT_ALARM_ID) {
                // populate the UI
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_ALARM_ID, mAlarmId);
        super.onSaveInstanceState(outState);
    }

    /**
     * initViews is called from onCreate to init the member variable views
     */
    private void initViews() {
        mLocation = findViewById(R.id.editTextAlarmLocation);
        mLatitude = findViewById(R.id.editTextAlarmLatitude);
        mLongitude = findViewById(R.id.editTextAlarmLongitude);
        mRadius = findViewById(R.id.editTextAlarmRadius);
        mEnabled = findViewById(R.id.editTextAlarmEnabled);
        mVibrate = findViewById(R.id.editTextAlarmVibrate);
        mMessage = findViewById(R.id.editTextAlarmMessage);
        mAlert = findViewById(R.id.editTextAlarmAlert);

        mButton = findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }

    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param alarm the alarmEntry to populate the UI
     */
    private void populateUI(AlarmEntry alarm) {

    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new alarm data into the underlying database.
     */
    public void onSaveButtonClicked() {
        String location = mLocation.getText().toString();
        double latitude = Double.parseDouble(mLatitude.getText().toString());
        double longitude = Double.parseDouble(mLongitude.getText().toString());
        double radius = Double.parseDouble(mRadius.getText().toString());
        boolean enabled = Boolean.parseBoolean(mEnabled.getText().toString());
        boolean vibrate = Boolean.parseBoolean(mVibrate.getText().toString());
        String message = mMessage.getText().toString();
        String alert = mAlert.getText().toString();

        final AlarmEntry alarmEntry = new AlarmEntry(location, latitude, longitude, radius,
                enabled, vibrate, message, alert);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.alarmDao().insertAlarm(alarmEntry);
                finish();
            }
        });
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
        Uri geoLocation = Uri.parse("geo:0,0?q=" + mLocation);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
