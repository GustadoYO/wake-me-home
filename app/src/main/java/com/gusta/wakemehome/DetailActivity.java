package com.gusta.wakemehome;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.gusta.wakemehome.maps.MapDestination;
import com.gusta.wakemehome.maps.MapsActivity;
import com.gusta.wakemehome.utilities.fileUtils;
import com.gusta.wakemehome.viewmodel.AppExecutors;
import com.gusta.wakemehome.viewmodel.DetailViewModel;
import com.gusta.wakemehome.viewmodel.DetailViewModelFactory;

import java.util.Objects;

import static com.gusta.wakemehome.utilities.fileUtils.isPathExists;

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
    public static final String INSTANCE_ALARM_RINGTONE = "instanceAlarmAlert";
    // save alarm address to be received after rotation
    public static final String INSTANCE_ALARM_ADDRESS_DATA = "instanceAlarmAddressData";

    // map intent request code
    private static final int MAP_REQUEST_CODE = 1;

    // ringtone picker request code
    private static final int RINGTONE_PICKER_REQUEST_CODE = 2;

    //=========//
    // MEMBERS //
    //=========//

    private MapDestination mMapDestination;                 // The current alarm address
    private int mAlarmId;                           // The current alarm id
    private Uri mAlarmRingtone;                     // The current alarm ringtone
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

        LinearLayout ringtone = mDetailBinding.clockDetails.alert;
        ringtone.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                pickRingtone();
            }

        });

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(INSTANCE_ALARM_ID))
                mAlarmId = savedInstanceState.getInt(INSTANCE_ALARM_ID, DEFAULT_ALARM_ID);
            if (savedInstanceState.containsKey(INSTANCE_ALARM_ADDRESS_DATA)) {
                mMapDestination = savedInstanceState.getParcelable(INSTANCE_ALARM_ADDRESS_DATA);
            }
            if (savedInstanceState.containsKey(INSTANCE_ALARM_RINGTONE)) {
                mAlarmRingtone = Uri.parse(savedInstanceState.getString(INSTANCE_ALARM_RINGTONE));
            }
        }

        // If ALARM_ID was sent, it is update mode (list item clicked)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ALARM_ID)) {
            setAlarmData(intent);
        }
        updateMapImage(true);
        setRingtoneName();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save alarm ID to state (to keep it in case of phone orientation change for example)
        outState.putParcelable(INSTANCE_ALARM_ADDRESS_DATA, mMapDestination);
        outState.putInt(INSTANCE_ALARM_ID, mAlarmId);
        if(mAlarmRingtone != null)
            outState.putString(INSTANCE_ALARM_RINGTONE, mAlarmRingtone.toString());
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
                    mMapDestination = new MapDestination(alarmEntry.getLatitude(), alarmEntry.getLongitude(), alarmEntry.getLocation(), alarmEntry.getRadius());
                    mAlarmRingtone = alarmEntry.getAlert() != null ? Uri.parse(alarmEntry.getAlert()) : null;
                    // populate the UI
                    populateUI(alarmEntry);
                }
            });
        }
    }

    private void updateMapImage(boolean deleteTemp) {

        ImageView mapsImage = mDetailBinding.locationDetails.mapImage;
        //delete older map to be able to update map for existing map
        if(deleteTemp){
            fileUtils.deleteTempImage();
        }
        String tempImagePath = fileUtils.getTempPath() ;
        //if there is temp it'll be selected
        //temp image will delete on enter to existing alarm or on back without
        //saving on map activity otherwise we will take the temp file to show up
        String imgPath = !isPathExists(tempImagePath) ? fileUtils.getMapImagePath(mAlarmId) : tempImagePath;

        if(!isPathExists(imgPath)){
            mapsImage.setVisibility(View.GONE);
        } else {

            mapsImage.setVisibility(View.VISIBLE);

            Bitmap myBitmap = BitmapFactory.decodeFile(imgPath);

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
        setRingtoneName();
        updateMapImage(true);
    }

    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new alarm data into the underlying database.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onSaveButtonClicked() {

        //check for getting address data from map at all
        if(mMapDestination == null){
            Toast.makeText(getApplicationContext(), R.string.error_mandatory, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        // Parse numeric fields to their appropriate types
        String location = mMapDestination.getLocation();
        float radius = mMapDestination.getRadius();
        double latitude = mMapDestination.getLatitude();
        double longitude = mMapDestination.getLongitude();
        String ringtone = mAlarmRingtone == null ? getDefaultRingtone().toString() : mAlarmRingtone.toString();

        boolean vibrate = mDetailBinding.clockDetails.vibrate.isChecked();
        String message = mDetailBinding.clockDetails.message.getText().toString();

        /*
        Validation check
         */

        // Show error and abort save if one of the mandatory fields is empty
        if (radius <= 0 || location == null) {
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
                    enabled, vibrate, message, ringtone);
        } else {
            alarm = new AlarmEntry(mAlarmId, location, latitude, longitude, radius,
                    enabled, vibrate, message, ringtone);
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
                fileUtils.saveMapImage(mAlarmId);
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

        // Create a new intent to start an map activity
        Intent mapIntent =
                new Intent(DetailActivity.this, MapsActivity.class);
        if (mMapDestination != null && mMapDestination.getLocation() != null && mMapDestination.getRadius() >= 0) {
            MapDestination mapDestination = new MapDestination(mMapDestination.getLatitude(), mMapDestination.getLongitude(), mMapDestination.getLocation(), mMapDestination.getRadius());
            mapIntent.putExtra(DetailActivity.EXTRA_ALARM_ADDRESS, mapDestination);
        }
        startActivityForResult(mapIntent, MAP_REQUEST_CODE);

    }

    /**
     * pick ringtone from default android ringtone selector
     * Note: this intent doesn't exist on the emulator
     */
    public void pickRingtone() {
        final Uri currentTone = getCurrentRingtone();
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, R.string.label_ringtone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        startActivityForResult(intent, RINGTONE_PICKER_REQUEST_CODE);
    }

    /**
     * default ringtone value of android
     * @return
     */
    private Uri getDefaultRingtone(){
        return RingtoneManager.getActualDefaultRingtoneUri(DetailActivity.this, RingtoneManager.TYPE_ALARM);
    }

    /**
     * current could be the saved value when exist or android default
     * @return
     */
    private Uri getCurrentRingtone(){
        return mAlarmRingtone != null ? mAlarmRingtone : getDefaultRingtone();
    }

    /**
     * set the ringtone name from relevant uri -> saved value or default ringtone value
     */
    private void setRingtoneName(){
        //select default ringtone when there isn't ringtone which chose
        Uri ringtoneUri = getCurrentRingtone();
        Ringtone ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        mDetailBinding.clockDetails.ringtone.setText(ringtone.getTitle(this));
    }

    /**
     * return results from activities
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RINGTONE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK ){
                mAlarmRingtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                setRingtoneName();
        }
        if (requestCode == MAP_REQUEST_CODE) {
            if (data != null && data.hasExtra(EXTRA_ALARM_ADDRESS)) {
                // get coordinates (from intent)
                mMapDestination = data.getParcelableExtra(EXTRA_ALARM_ADDRESS);
            }
        }
        updateMapImage(false);
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
