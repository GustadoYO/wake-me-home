package com.gusta.wakemehome;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;
import com.gusta.wakemehome.viewmodel.AppExecutors;
import com.gusta.wakemehome.viewmodel.MainViewModel;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends AppCompatActivity implements
        AlarmAdapter.ItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    //===========//
    // CONSTANTS //
    //===========//

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();

    //===========//
    // VARIABLES //
    //===========//

    private AlarmAdapter mAdapter;  // The RecyclerView adapter
    private AppDatabase mDb;        // The database member

    //=========//
    // METHODS //
    //=========//

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
                        mDb.alarmDao().deleteAlarm(alarms.get(position));
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        Log.d(TAG, "onCreate: registering preference changed listener");

        /*
         * Register MainActivity as an OnPreferenceChangedListener to receive a callback when a
         * SharedPreference has changed. Please note that we must unregister MainActivity as an
         * OnSharedPreferenceChanged listener in onDestroy to avoid any memory leaks.
         */
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

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
        mDb = AppDatabase.getInstance(getApplicationContext());
        setupViewModel();
    }

    /**
     * Start observing the "alarms list" model in order to  update UI of any change
     * */
    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getAlarms().observe(this, new Observer<List<AlarmEntry>>() {
            @Override
            public void onChanged(@Nullable List<AlarmEntry> alarmEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setAlarms(alarmEntries);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks. */
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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

    //==========================================//
    // OnSharedPreferenceChangeListener METHODS //
    //==========================================//

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        mAdapter.notifyDataSetChanged();
    }
}
