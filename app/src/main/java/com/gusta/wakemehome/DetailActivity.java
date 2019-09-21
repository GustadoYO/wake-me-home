package com.gusta.wakemehome;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;
import com.gusta.wakemehome.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity {

    //===========//
    // CONSTANTS //
    //===========//

    // Constant for logging
    private static final String TAG = DetailActivity.class.getSimpleName();
    // Extra for the alarm ID to be received in the intent
    public static final String EXTRA_ALARM_ID = "extraAlarmId";
    // Extra for the alarm ID to be received after rotation
    public static final String INSTANCE_ALARM_ID = "instanceAlarmId";
    // Constant for default alarm id to be used when not in update mode
    private static final int DEFAULT_ALARM_ID = -1;

    //===========//
    // VARIABLES //
    //===========//

    Button mButton;                                 // The save button
    private int mAlarmId = DEFAULT_ALARM_ID;        // The current alarm ID
    private AppDatabase mDb;                        // The database member
    private ActivityDetailBinding mDetailBinding;   // The data binding object

    //=========//
    // METHODS //
    //=========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init the data binding object
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Init the save button
        mButton = mDetailBinding.saveButton;
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });

        // Init the database member
        mDb = AppDatabase.getInstance(getApplicationContext());

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ALARM_ID)) {
            mAlarmId = savedInstanceState.getInt(INSTANCE_ALARM_ID, DEFAULT_ALARM_ID);
        }

        // If ALARM_ID was sent, it is update mode (list item clicked)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ALARM_ID)) {
            mButton.setText(R.string.update_button);

            // If member alarm ID is still DEFAULT_ID, the alarm fields should be loaded from model
            if (mAlarmId == DEFAULT_ALARM_ID) {

                // Set member alarm ID to wanted alarm (from intent)
                mAlarmId = intent.getIntExtra(EXTRA_ALARM_ID, DEFAULT_ALARM_ID);

                // Load alarm model
                DetailViewModelFactory factory = new DetailViewModelFactory(mDb, mAlarmId);
                final DetailViewModel viewModel =
                        ViewModelProviders.of(this, factory).get(DetailViewModel.class);

                // Observe changes in model in order to update UI
                viewModel.getAlarm().observe(this, new Observer<AlarmEntry>() {
                    @Override
                    public void onChanged(@Nullable AlarmEntry alarmEntry) {
                        // populate the UI
                        viewModel.getAlarm().removeObserver(this);
                        populateUI(alarmEntry);
                    }
                });
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save alarm ID to state (to keep it in case of phone orientation change for example)
        outState.putInt(INSTANCE_ALARM_ID, mAlarmId);
        super.onSaveInstanceState(outState);
    }

    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param alarm the alarmEntry to populate the UI
     */
    private void populateUI(AlarmEntry alarm) {
        if (alarm == null) {
            return;
        }

        mDetailBinding.locationDetails.location.setText(alarm.getLocation());
        mDetailBinding.locationDetails.latitude.setText(String.valueOf(alarm.getLatitude()));
        mDetailBinding.locationDetails.longitude.setText(String.valueOf(alarm.getLongitude()));
        mDetailBinding.locationDetails.radius.setText(String.valueOf(alarm.getRadius()));
        mDetailBinding.clockDetails.vibrate.setChecked(alarm.isVibrate());
        mDetailBinding.clockDetails.message.setText(alarm.getMessage());
        mDetailBinding.clockDetails.alert.setText(alarm.getAlert());
    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new alarm data into the underlying database.
     */
    public void onSaveButtonClicked() {
        String location = mDetailBinding.locationDetails.location.getText().toString();
        double latitude =
                Double.parseDouble(mDetailBinding.locationDetails.latitude.getText().toString());
        double longitude =
                Double.parseDouble(mDetailBinding.locationDetails.longitude.getText().toString());
        double radius =
                Double.parseDouble(mDetailBinding.locationDetails.radius.getText().toString());
//        boolean enabled =
//                  Boolean.parseBoolean(mEnabled.getText().toString());
        boolean vibrate =
                Boolean.parseBoolean(mDetailBinding.clockDetails.vibrate.getText().toString());
        String message =
                mDetailBinding.clockDetails.message.getText().toString();
        String alert =
                mDetailBinding.clockDetails.alert.getText().toString();

        // TODO: replace "true" constant with the current "enabled" value
        final AlarmEntry alarm = new AlarmEntry(location, latitude, longitude, radius,
                true, vibrate, message, alert);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mAlarmId == DEFAULT_ALARM_ID) {
                    // insert new alarm
                    mDb.alarmDao().insertAlarm(alarm);
                } else {
                    //update task
                    alarm.setId(mAlarmId);
                    mDb.alarmDao().updateAlarm(alarm);
                }
                finish();
            }
        });
    }

    //=====================//
    // OPTION MENU METHODS //
    //=====================//

    /**
     * This method uses the URI scheme for showing the alarm on a
     * map. This super-handy intent is detailed in the "Common Intents"
     * page of Android's developer site:
     *
     * see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
     *
     * Hint: Hold Command on Mac or Control on Windows and click that link
     * to automagically open the Common Intents page
     *
     * @return The Intent to use to open the map.
     */
    private Intent createOpenAlarmInMapIntent() {
        Uri geoLocation =
                Uri.parse("geo:0,0?q=" + mDetailBinding.locationDetails.location.getText());

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
