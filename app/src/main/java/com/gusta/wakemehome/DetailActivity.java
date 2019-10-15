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
import android.widget.TextView;
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
    // Extra for the alarm ID to be received in the intent
    public static final String ALARM_COORDINATES = "alarmCoordinates";
    // Extra for the alarm ID to be received after rotation
    public static final String INSTANCE_ALARM_ID = "instanceAlarmId";
    // Constant for default alarm id to be used when not in update mode

    //=========//
    // MEMBERS //
    //=========//

    private AlarmEntry mAlarmEntry;                 // The current alarm entry
    private DetailViewModel mViewModel;             // The current alarm view model
    private AppDatabase mDb;                        // The database member
    private ActivityDetailBinding mDetailBinding;   // The data binding object

    //=========//
    // METHODS //
    //=========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmEntry = new AlarmEntry();

        // Init the data binding object
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Init the save button
        Button mMapButton = mDetailBinding.OpenMapButton;
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOpenMapButtonClicked();
            }
        });

        // Init the save button
        Button mAlertButton = mDetailBinding.clockDetails.alert;
        mAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRingtoneSelect();
            }
        });

        // Init the database member
        mDb = AppDatabase.getInstance(getApplicationContext());

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ALARM_ID)) {
            mAlarmEntry.setId(savedInstanceState.getInt(INSTANCE_ALARM_ID, AlarmEntry.DEFAULT_ALARM_ID));
        }

        // If ALARM_ID was sent, it is update mode (list item clicked)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(ALARM_COORDINATES)) {
            // get coordinates (from intent)
            MapAddress mapAddress = intent.getParcelableExtra(ALARM_COORDINATES);
            getAlarmData(intent,mapAddress);
        }
        if (intent != null && intent.hasExtra(EXTRA_ALARM_ID)) {
            getAlarmData(intent,null);
        }
        updateUIVisibility();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save alarm ID to state (to keep it in case of phone orientation change for example)
        outState.putInt(INSTANCE_ALARM_ID, mAlarmEntry.getId());
        super.onSaveInstanceState(outState);
    }

    private void getAlarmData(Intent intent, final MapAddress mapAddress){
        // If member alarm ID is still DEFAULT_ID, the alarm model should be loaded from db
        if (mAlarmEntry.getId() == AlarmEntry.DEFAULT_ALARM_ID) {

            // Set member alarm ID to wanted alarm (from intent)
            mAlarmEntry.setId(intent.getIntExtra(EXTRA_ALARM_ID, AlarmEntry.DEFAULT_ALARM_ID));

            // Load alarm model
            DetailViewModelFactory factory = new DetailViewModelFactory(mDb, mAlarmEntry.getId());
            mViewModel =
                    ViewModelProviders.of(this, factory).get(DetailViewModel.class);

            // Observe changes in model in order to update UI
            mViewModel.getAlarm().observe(this, new Observer<AlarmEntry>() {
                @Override
                public void onChanged(@Nullable AlarmEntry alarmEntry) {
                    // populate the UI
                    mViewModel.getAlarm().removeObserver(this);
                    if(alarmEntry != null) {
                        mAlarmEntry = alarmEntry;
                    }
                    if(mapAddress != null) {
                        mAlarmEntry.setLocation(mapAddress.getLocation());
                        mAlarmEntry.setLongitude(mapAddress.getLongitude());
                        mAlarmEntry.setLatitude(mapAddress.getLatitude());
                        mAlarmEntry.setRadius(mapAddress.getRadius());
                    }
                    populateUI(mAlarmEntry);
                }
            });
        }
    }

    private void updateUIVisibility(){
        TextView locationTextView = mDetailBinding.location;
        TextView locationTextViewLabel = mDetailBinding.locationLabel;
        if(mAlarmEntry.getLocation() == null) {
            locationTextView.setVisibility(View.GONE);
            locationTextViewLabel.setVisibility(View.GONE);
        }else {
            locationTextView.setVisibility(View.VISIBLE);
            locationTextViewLabel.setVisibility(View.VISIBLE);
        }
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
        mDetailBinding.location.setText(alarm.getLocation());
        mDetailBinding.clockDetails.vibrate.setChecked(alarm.isVibrate());
        mDetailBinding.clockDetails.message.setText(alarm.getMessage());
        mDetailBinding.clockDetails.alert.setText(alarm.getAlert());
        updateUIVisibility();
    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new alarm data into the underlying database.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onSaveButtonClicked() {
        // Get user inputs
        mAlarmEntry.setVibrate(mDetailBinding.clockDetails.vibrate.isChecked());
        mAlarmEntry.setMessage(mDetailBinding.clockDetails.message.getText().toString());
        mAlarmEntry.setAlert(mDetailBinding.clockDetails.alert.getText().toString());

        // Show error and abort save if one of the mandatory fields is empty
        if (!mAlarmEntry.isValidEntry()) {
            Toast.makeText(getApplicationContext(),R.string.error_mandatory,Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Parse numeric fields to their appropriate types
        double latitude = Double.parseDouble(latitudeString);
        double longitude = Double.parseDouble(longitudeString);
        float radius = Float.parseFloat(radiusString);

        // Check if coordinates are valid: -90 < latitude < 90 && -180 < longitude < 180
        if (latitude < -90 || latitude > 90) {
            Toast.makeText(getApplicationContext(),
                    R.string.error_latitude,Toast.LENGTH_SHORT).show();
            return;
        }
        if (longitude < -180 || longitude > 180) {
            Toast.makeText(getApplicationContext(),
                    R.string.error_longitude,Toast.LENGTH_SHORT).show();
            return;
        }

        // "enabled" field is not shown on this screen - keep current value (if exists)
        boolean enabled = (mViewModel == null) ||
                Objects.requireNonNull(mViewModel.getAlarm().getValue()).isEnabled();

        // Save the added/updated alarm entity
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if(mAlarmEntry.isNewEntry()){
                    mDb.alarmDao().insertAlarm(mAlarmEntry);
                }else{
                    mDb.alarmDao().updateAlarm(mAlarmEntry);
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

        // Create a new intent to start an DetailActivity
        Intent addTaskIntent =
                new Intent(DetailActivity.this, MapsActivity.class);
        addTaskIntent.putExtra(EXTRA_ALARM_ID, mAlarmEntry.getId());
        startActivity(addTaskIntent);
        finish();
    }

    private void onRingtoneSelect() {
        Intent intent_upload = new Intent();
        intent_upload.setType("audio/*");
        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent_upload, 1);
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
