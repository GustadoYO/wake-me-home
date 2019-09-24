package com.gusta.wakemehome;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;
import com.gusta.wakemehome.databinding.ActivityDetailBinding;
import com.gusta.wakemehome.viewmodel.AppExecutors;
import com.gusta.wakemehome.viewmodel.DetailViewModel;
import com.gusta.wakemehome.viewmodel.DetailViewModelFactory;

import java.util.Objects;

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

    private int mAlarmId = DEFAULT_ALARM_ID;        // The current alarm ID
    private DetailViewModel mViewModel;             // The current alarm view model
    private AppDatabase mDb;                        // The database member
    private ActivityDetailBinding mDetailBinding;   // The data binding object
    Button mButton;                                 // The open map button

    //=========//
    // METHODS //
    //=========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init the data binding object
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Init the save button
        mButton = mDetailBinding.locationDetails.OpenMapButton;
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOpenMapButtonClicked();
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

            // If member alarm ID is still DEFAULT_ID, the alarm model should be loaded from db
            if (mAlarmId == DEFAULT_ALARM_ID) {

                // Set member alarm ID to wanted alarm (from intent)
                mAlarmId = intent.getIntExtra(EXTRA_ALARM_ID, DEFAULT_ALARM_ID);

                // Load alarm model
                DetailViewModelFactory factory = new DetailViewModelFactory(mDb, mAlarmId);
                mViewModel =
                        ViewModelProviders.of(this, factory).get(DetailViewModel.class);

                // Observe changes in model in order to update UI
                mViewModel.getAlarm().observe(this, new Observer<AlarmEntry>() {
                    @Override
                    public void onChanged(@Nullable AlarmEntry alarmEntry) {
                        // populate the UI
                        mViewModel.getAlarm().removeObserver(this);
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
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onSaveButtonClicked() {
        // Get user inputs
        String location = mDetailBinding.locationDetails.location.getText().toString();
        String latitudeString = mDetailBinding.locationDetails.latitude.getText().toString();
        String longitudeString = mDetailBinding.locationDetails.longitude.getText().toString();
        String radiusString = mDetailBinding.locationDetails.radius.getText().toString();
        boolean vibrate = mDetailBinding.clockDetails.vibrate.isChecked();
        String message = mDetailBinding.clockDetails.message.getText().toString();
        String alert = mDetailBinding.clockDetails.alert.getText().toString();

        // Show error and abort save if one of the mandatory fields is empty
        if (latitudeString.isEmpty() || longitudeString.isEmpty() || radiusString.isEmpty()) {
            Toast.makeText(getApplicationContext(),R.string.mandatory_fields,Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Parse numeric fields to their appropriate types
        double latitude = Double.parseDouble(latitudeString);
        double longitude = Double.parseDouble(longitudeString);
        double radius = Double.parseDouble(radiusString);

        // "enabled" field is not shown on this screen - keep current value (if exists)
        boolean enabled = (mViewModel == null) ||
                Objects.requireNonNull(mViewModel.getAlarm().getValue()).isEnabled();

        // Save the added/updated alarm entity
        final AlarmEntry alarm = new AlarmEntry(location, latitude, longitude, radius,
                enabled, vibrate, message, alert);
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

    /**
     * This method uses the URI scheme for showing the alarm on a
     * map. This super-handy intent is detailed in the "Common Intents"
     * page of Android's developer site:
     *
     * see <a"http://developer.android.com/guide/components/intents-common.html#Maps">
     *
     * Hint: Hold Command on Mac or Control on Windows and click that link
     * to automatically open the Common Intents page
     */
    private void onOpenMapButtonClicked() {
        Uri geoLocation =
                Uri.parse("geo:0,0?q=" + mDetailBinding.locationDetails.location.getText());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString() +
                    ", no receiving apps installed!");
        }
    }

    //=====================//
    // OPTION MENU METHODS //
    //=====================//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            onSaveButtonClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
