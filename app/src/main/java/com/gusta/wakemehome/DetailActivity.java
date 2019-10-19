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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.databinding.ActivityDetailBinding;
import com.gusta.wakemehome.maps.MapAddress;
import com.gusta.wakemehome.maps.MapsActivity;
import com.gusta.wakemehome.viewmodel.AppExecutors;
import com.gusta.wakemehome.viewmodel.DetailViewModel;

import java.io.File;
import java.util.Objects;

import static com.gusta.wakemehome.database.AlarmEntry.DEFAULT_ALARM_ID;

public class DetailActivity extends AppCompatActivity {

    //===========//
    // CONSTANTS //
    //===========//

    // Constant for logging
    private static final String TAG = DetailActivity.class.getSimpleName();
    //Extra alarm id from main activity
    public static final String EXTRA_ALARM_ID = "extraAlarmId";
    //extra alarm from map provider
    public static final String EXTRA_ALARM_COORDINATES = "alarmCoordinates";
    //ui element which suppose to save on rotate
    public static final String INSTANCE_ALARM_ID = "instanceAlarmId";
    public static final String INSTANCE_ALARM_MESSAGE = "instanceAlarmMessage";
    public static final String INSTANCE_ALARM_ALERT = "instanceAlarmAlert";
    public static final String INSTANCE_ALARM_VIBRATE = "instanceAlarmVibrate";

    //temp png will be for unsaved snapshots on save it'll change to  map id.png
    public static final String TEMP_IMAGE_FILE = "temp.png";
    // map intent request code
    private static final int MAP_REQUEST_CODE = 1;

    //=========//
    // MEMBERS //
    //=========//

    private AlarmEntry mAlarmEntry;                 // The current alarm entry
    private DetailViewModel mViewModel;             // The current alarm view model
    private ActivityDetailBinding mDetailBinding;   // The data binding object

    //=========//
    // METHODS //
    //=========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlarmEntry = new AlarmEntry();

        mViewModel = ViewModelProviders.of(this).get(DetailViewModel.class);

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

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_ALARM_ID)) {
            mAlarmEntry.setId(savedInstanceState.getInt(INSTANCE_ALARM_ID, DEFAULT_ALARM_ID));
            mAlarmEntry.setMessage(savedInstanceState.getString(INSTANCE_ALARM_MESSAGE, ""));
            mAlarmEntry.setVibrate(savedInstanceState.getBoolean(INSTANCE_ALARM_VIBRATE, false));
            mAlarmEntry.setAlert(savedInstanceState.getString(INSTANCE_ALARM_ALERT, null));
        }

        // If ALARM_ID was sent, it is update mode (list item clicked)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ALARM_ID)) {
            setAlarmData(intent);
        }
//        updateLocation();
        updateMapImage();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save alarm ID to state (to keep it in case of phone orientation change for example)
        outState.putInt(INSTANCE_ALARM_ID, mAlarmEntry.getId());
        outState.putString(INSTANCE_ALARM_MESSAGE, mDetailBinding.clockDetails.message.getText().toString());
        outState.putString(INSTANCE_ALARM_ALERT, mAlarmEntry.getAlert());
        outState.putBoolean(INSTANCE_ALARM_VIBRATE, mDetailBinding.clockDetails.vibrate.isChecked());
        super.onSaveInstanceState(outState);
    }

    private void setAlarmData(Intent intent){
        // If member alarm ID is still DEFAULT_ID, the alarm model should be loaded from db
        if (mAlarmEntry.getId() == DEFAULT_ALARM_ID) {

            // Observe changes in model in order to update UI
            mViewModel.getAlarm(intent.getIntExtra(EXTRA_ALARM_ID, DEFAULT_ALARM_ID)).observe(this, new Observer<AlarmEntry>() {
                @Override
                public void onChanged(@Nullable AlarmEntry alarmEntry) {
                    mViewModel.getAlarm().removeObserver(this);
                    mAlarmEntry = alarmEntry;
                    // populate the UI
                    populateUI(mAlarmEntry);
                }
            });
        }
    }

    private void updateLocation(){
        TextView locationTextView = mDetailBinding.location;
        TextView locationTextViewLabel = mDetailBinding.locationLabel;
        if(mAlarmEntry.getLocation() == null) {
            locationTextView.setVisibility(View.GONE);
            locationTextViewLabel.setVisibility(View.GONE);
        }else {
            locationTextView.setVisibility(View.VISIBLE);
            locationTextView.setText(mAlarmEntry.getLocation());
            locationTextViewLabel.setVisibility(View.VISIBLE);
        }
    }

    private void updateMapImage(){
        ImageView mapsImage = mDetailBinding.mapImage;
        if (mAlarmEntry.getImage() == null){
            mapsImage.setVisibility(View.GONE);
        }else{
            mapsImage.setVisibility(View.VISIBLE);

            File imgFile = new File(mAlarmEntry.getImage());
            if(imgFile.exists()){

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                mapsImage.setImageBitmap(myBitmap);

            }
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
//        mDetailBinding.location.setText(alarm.getLocation());
        mDetailBinding.clockDetails.vibrate.setChecked(alarm.isVibrate());
        mDetailBinding.clockDetails.message.setText(alarm.getMessage());
//        updateLocation();
        updateMapImage();
    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new alarm data into the underlying database.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onSaveButtonClicked() {
        // Get user inputs
        // Parse numeric fields to their appropriate types
        double latitude = mAlarmEntry.getLatitude();
        double longitude = mAlarmEntry.getLongitude();

        mAlarmEntry.setVibrate(mDetailBinding.clockDetails.vibrate.isChecked());
        mAlarmEntry.setMessage(mDetailBinding.clockDetails.message.getText().toString());
        mAlarmEntry.setAlert(mDetailBinding.clockDetails.alert.getText().toString());

        // Show error and abort save if one of the mandatory fields is empty
        if (mAlarmEntry.getRadius() == 0 || mAlarmEntry.getLocation() == null) {
            Toast.makeText(getApplicationContext(),R.string.error_mandatory,Toast.LENGTH_SHORT)
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
        boolean enabled = (mViewModel == null) ||
                Objects.requireNonNull(mAlarmEntry.isEnabled());

        mAlarmEntry.setEnabled(enabled);
        // Save the added/updated alarm entity
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if(mAlarmEntry.getId() == DEFAULT_ALARM_ID){
                    mAlarmEntry.setImage(setFileNameForSnapshot(mAlarmEntry));
                    mViewModel.insertAlarm(mAlarmEntry);
                }else{
                    mViewModel.updateAlarm(mAlarmEntry);

                }
                finish();
            }
        });
    }
    private String setFileNameForSnapshot(AlarmEntry alarm){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());

        File directory = cw.getDir("mapsDir", Context.MODE_PRIVATE);
        // Create imageDir
        File source = new File(alarm.getImage());

        // File (or directory) with new name
        File dest = new File(directory,alarm.getId() + ".png");

        if (dest.exists())
            dest.delete();

        // Rename file (or directory)
        source.renameTo(dest);
        return dest.getAbsolutePath();
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
        if (mAlarmEntry != null && mAlarmEntry.getLocation() != null && mAlarmEntry.getRadius() > 0){
            MapAddress mapAddress = new MapAddress(mAlarmEntry.getLatitude(), mAlarmEntry.getLongitude(), mAlarmEntry.getLocation(),mAlarmEntry.getRadius());
            mapIntent.putExtra(DetailActivity.EXTRA_ALARM_COORDINATES, mapAddress);
        }
        startActivityForResult(mapIntent, MAP_REQUEST_CODE);

    }

    // Call Back method  to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MAP_REQUEST_CODE)
        {
            if (data != null && data.hasExtra(EXTRA_ALARM_COORDINATES)) {
                // get coordinates (from intent)
                MapAddress mapAddress = data.getParcelableExtra(EXTRA_ALARM_COORDINATES);
                // Set member alarm ID to wanted alarm (from intent)
                if(mapAddress != null) {
                    mAlarmEntry.setLocation(mapAddress.getLocation());
                    mAlarmEntry.setLatitude(mapAddress.getLatitude());
                    mAlarmEntry.setLongitude(mapAddress.getLongitude());
                    mAlarmEntry.setRadius(mapAddress.getRadius());
                    mAlarmEntry.setImage(mapAddress.getLocationImgUri());
                }
            }
        }
//        updateLocation();
        updateMapImage();
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
