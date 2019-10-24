package com.gusta.wakemehome;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.gusta.wakemehome.maps.MapAddress;
import com.gusta.wakemehome.maps.MapsActivity;
import com.gusta.wakemehome.viewmodel.AppExecutors;
import com.gusta.wakemehome.viewmodel.DetailViewModel;
import com.gusta.wakemehome.viewmodel.DetailViewModelFactory;

import java.io.File;
import java.util.Objects;

public class DetailActivity extends AppCompatActivity {

    //===========//
    // CONSTANTS //
    //===========//

    // Constant for logging
    private static final String TAG = DetailActivity.class.getSimpleName();
    //default value for alarm id
    public static final int DEFAULT_ALARM_ID = -1;
    // Extra for the alarm ID to be received in the intent
    public static final String EXTRA_ALARM_ID = "extraAlarmId";
    // Extra for alarm address object from map provider
    public static final String EXTRA_ALARM_ADDRESS = "alarmCoordinates";
    // save alarm ID to be received after rotation
    public static final String INSTANCE_ALARM_ID = "instanceAlarmId";
    // save alarm alert to be received after rotation
    public static final String INSTANCE_ALARM_ALERT = "instanceAlarmAlert";
    // save alarm address to be received after rotation
    public static final String INSTANCE_ALARM_ADDRESS_DATA = "instanceAlarmAddressData";

    //temp png will be for unsaved snapshots on save it'll change to  map id.png
    public static final String TEMP_IMAGE_FILE = "temp.png";
    // map intent request code
    private static final int MAP_REQUEST_CODE = 1;

    //=========//
    // MEMBERS //
    //=========//

    private MapAddress mMapAddress;                 // The current alarm address
    private int mAlarmId;                           // The current alarm id
    //TODO add alert
    private String mAlarmAlert;                     // The current alarm alert
    private DetailViewModel mViewModel;             // The current alarm view model
    private ActivityDetailBinding mDetailBinding;   // The data binding object
    private AppDatabase mDb;                        // db instance

    //=========//
    // METHODS //
    //=========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmId = DEFAULT_ALARM_ID;

        mDb = AppDatabase.getInstance(this);

        // Init the data binding object
        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Init the save button
        Button mMapButton = mDetailBinding.locationDetails.OpenMapButton;
        mMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOpenMapButtonClicked();
            }
        });

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(INSTANCE_ALARM_ID))
                mAlarmId = savedInstanceState.getInt(INSTANCE_ALARM_ID, DEFAULT_ALARM_ID);
            if (savedInstanceState.containsKey(INSTANCE_ALARM_ADDRESS_DATA)) {
                mMapAddress = savedInstanceState.getParcelable(INSTANCE_ALARM_ADDRESS_DATA);
            }
        }

        // If ALARM_ID was sent, it is update mode (list item clicked)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ALARM_ID)) {
            //delete older map to be able to update map for existing map
            deleteTempFile();
            setAlarmData(intent);
        }
        updateMapImage(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save alarm ID to state (to keep it in case of phone orientation change for example)
        outState.putParcelable(INSTANCE_ALARM_ADDRESS_DATA, mMapAddress);
        outState.putInt(INSTANCE_ALARM_ID, mAlarmId);
        super.onSaveInstanceState(outState);
    }

    private void setAlarmData(Intent intent) {
        // If member alarm is new and there is ID from intent it should load from db
        if (mAlarmId == DEFAULT_ALARM_ID) {

            mAlarmId = intent.getIntExtra(EXTRA_ALARM_ID, DEFAULT_ALARM_ID);
            // factory view model is used for sending parameters to the view model in our case
            // with alarm entry id to make sure we have one entity on our viewModel
            DetailViewModelFactory factory = new DetailViewModelFactory(mDb, mAlarmId);
            mViewModel =
                    ViewModelProviders.of(this, factory).get(DetailViewModel.class);

            // Observe changes in model in order to update UI
            mViewModel.getAlarm().observe(this, new Observer<AlarmEntry>() {
                @Override
                public void onChanged(@Nullable AlarmEntry alarmEntry) {
                    mViewModel.getAlarm().removeObserver(this);
                    if (alarmEntry == null) {
                        return;
                    }
                    mAlarmId = alarmEntry.getId();
                    mMapAddress = new MapAddress(alarmEntry.getLatitude(), alarmEntry.getLongitude(), alarmEntry.getLocation(), alarmEntry.getRadius());
                    // populate the UI
                    populateUI(alarmEntry);
                }
            });
        }
    }

    private void updateMapImage(boolean showSavedMap) {
        ImageView mapsImage = mDetailBinding.locationDetails.mapImage;
        String imgPath = (showSavedMap) ? getImagePath(mAlarmId) : getLocalMapDir() + "/" + TEMP_IMAGE_FILE;
        File imgFile = new File(imgPath);
        if (!imgFile.exists()) {
            mapsImage.setVisibility(View.GONE);
        } else {
            mapsImage.setVisibility(View.VISIBLE);

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            mapsImage.setImageBitmap(myBitmap);

        }
    }

    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param alarm the alarmEntry to populate the UI
     */
    private void populateUI(AlarmEntry alarm) {
        mDetailBinding.clockDetails.vibrate.setChecked(alarm.isVibrate());
        mDetailBinding.clockDetails.message.setText(alarm.getMessage());
        updateMapImage(true);
    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new alarm data into the underlying database.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onSaveButtonClicked() {

        //check for getting address data from map at all
        if(mMapAddress == null){
            Toast.makeText(getApplicationContext(), R.string.error_mandatory, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Parse numeric fields to their appropriate types
        String location = mMapAddress.getLocation();
        float radius = mMapAddress.getRadius();
        double latitude = mMapAddress.getLatitude();
        double longitude = mMapAddress.getLongitude();
        boolean vibrate = mDetailBinding.clockDetails.vibrate.isChecked();
        String message = mDetailBinding.clockDetails.message.getText().toString();

        /*
        Validation check
         */

        // Show error and abort save if one of the mandatory fields is empty
        if (radius == 0 || location == null) {
            Toast.makeText(getApplicationContext(), R.string.error_mandatory, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

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
        boolean enabled = (mViewModel == null) || (mViewModel.getAlarm() == null) ||
                Objects.requireNonNull(mViewModel.getAlarm().getValue()).isEnabled();

        // Save the added/updated alarm entity
        //TODO add alert
        final AlarmEntry alarm;
        if (mAlarmId == DEFAULT_ALARM_ID) {
            alarm = new AlarmEntry(location, latitude, longitude, radius,
                    enabled, vibrate, message, null);
        } else {
            alarm = new AlarmEntry(mAlarmId, location, latitude, longitude, radius,
                    enabled, vibrate, message, null);
        }
        // Save the added/updated alarm entity
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (mAlarmId == DEFAULT_ALARM_ID) {
                    mAlarmId = (int) mDb.alarmDao().insertAlarm(alarm);
                } else {
                    mDb.alarmDao().updateAlarm(alarm);
                }
                updateSnapshotToAlarm(mAlarmId);
                finish();
            }
        });
    }

    //TODO: Move it to utils
    private void updateSnapshotToAlarm(int id) {
        // Create imageDir
        File source = new File(getLocalMapDir() + "/" + TEMP_IMAGE_FILE);

        // File (or directory) with new name
        File dest = new File(getImagePath(id));

        if (!source.exists())
            return;

        if (dest.exists())
            dest.delete();

        // Rename file (or directory)
        source.renameTo(dest);
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


        // Create a new intent to start an map activity
        Intent mapIntent =
                new Intent(DetailActivity.this, MapsActivity.class);
        if (mMapAddress != null && mMapAddress.getLocation() != null && mMapAddress.getRadius() > 0) {
            MapAddress mapAddress = new MapAddress(mMapAddress.getLatitude(), mMapAddress.getLongitude(), mMapAddress.getLocation(), mMapAddress.getRadius());
            mapIntent.putExtra(DetailActivity.EXTRA_ALARM_ADDRESS, mapAddress);
        }
        startActivityForResult(mapIntent, MAP_REQUEST_CODE);

    }

    // Call Back method to get map details from map activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE) {
            if (data != null && data.hasExtra(EXTRA_ALARM_ADDRESS)) {
                // get coordinates (from intent)
                mMapAddress = data.getParcelableExtra(EXTRA_ALARM_ADDRESS);
            }
        }
        updateMapImage(false);
    }

    //TODO use select ringtone method
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

    private String getLocalMapDir() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = cw.getDir("mapsDir", Context.MODE_PRIVATE);
        return directory.getAbsolutePath();
    }

    private String getImagePath(int id) {
        return getLocalMapDir() + "/" + id + ".png";
    }

    private void deleteTempFile() {
        File source = new File(getLocalMapDir() + "/" + TEMP_IMAGE_FILE);
        if (source.exists())
            source.delete();
    }
}
