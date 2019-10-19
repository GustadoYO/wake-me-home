package com.gusta.wakemehome;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.geofencing.GeofenceManager;
import com.gusta.wakemehome.viewmodel.AppExecutors;
import com.gusta.wakemehome.viewmodel.MainViewModel;

import java.util.List;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;
import static com.gusta.wakemehome.utilities.Constants.ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements
        AlarmAdapter.AlarmAdapterListeners{

    //===========//
    // CONSTANTS //
    //===========//

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    //=========//
    // MEMBERS //
    //=========//

    private AlarmAdapter mAdapter;              // The RecyclerView adapter
    private GeofenceManager mGeofenceManager;   // The geofence manager
    private MainViewModel mViewModel;           // The view model

    //===================//
    // LIFECYCLE METHODS //
    //===================//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the RecyclerView to its corresponding view
        // Member variables for the adapter and RecyclerView
        RecyclerView mRecyclerView = findViewById(R.id.rv_alarms);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new AlarmAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        // Add divider between list items
        DividerItemDecoration decoration =
                new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // call the diskIO execute method with a new Runnable and implement its run method
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<AlarmEntry> alarms = mAdapter.getAlarms();
                        AlarmEntry alarm = alarms.get(position);
                        mViewModel.deleteAlarm(alarm);
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        Log.d(TAG, "onCreate: registering preference changed listener");

        // Initialize the FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an DetailActivity
                Intent addTaskIntent =
                        new Intent(MainActivity.this, DetailActivity.class);
                startActivity(addTaskIntent);
            }
        });

        // Init the database member and the model
        setupViewModel();
    }

    /**
     * Start observing the "alarms list" model in order to update UI and geofences of any change
     * */
    private void setupViewModel() {
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if (mGeofenceManager == null)
            mGeofenceManager = new GeofenceManager(this, mViewModel.getAlarms());

        mViewModel.getAlarms().observe(this, new Observer<List<AlarmEntry>>() {
            @Override
            public void onChanged(@Nullable List<AlarmEntry> alarmEntries) {
                // Update UI
                Log.d(TAG, "Updating list of alarms from LiveData in ViewModel");
                mAdapter.setAlarms(alarmEntries);

                // Update geofence manger to current alarms list
                mGeofenceManager.updateGeofences();
            }
        });
    }

    //=====================//
    // OPTION MENU METHODS //
    //=====================//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //====================//
    // PERMISSION METHODS //
    //====================//

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == ACCESS_FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            mGeofenceManager.onRequestPermissionsResult(grantResults);
        }
    }

    //===========================//
    // ItemClickListener METHODS //
    //===========================//

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param itemId The alarm that was clicked
     */
    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ALARM_ID, itemId);
        startActivity(intent);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * enable.
     *
     * @param alarm The alarm that was enabled
     */
    @Override
    public void onAlarmEnabledChangeListener(final AlarmEntry alarm) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mViewModel.updateAlarm(alarm);
            }
        });
    }

}
