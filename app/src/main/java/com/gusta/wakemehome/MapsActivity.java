package com.gusta.wakemehome;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.gusta.wakemehome.DetailActivity.DEFAULT_ALARM_ID;
import static com.gusta.wakemehome.DetailActivity.EXTRA_ALARM_ID;


public class MapsActivity extends AppCompatActivity{

    public static final String INSTANCE_MAPS_SELECTION = "instanceMapsSelection";

    private Button mUpdateLocationButton;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private MapAddress mMapAddress;
    private MapProvider mMapProvider;
    private int mAlarmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init the data binding object
        setContentView(R.layout.activity_maps);

        mMapProvider = new GoogleMaps(this);

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MAPS_SELECTION)) {
            mMapAddress = savedInstanceState.getParcelable(INSTANCE_MAPS_SELECTION);
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ALARM_ID)) {
            // Set member alarm ID to wanted alarm (from intent)
            mAlarmId = intent.getIntExtra(EXTRA_ALARM_ID,DEFAULT_ALARM_ID);
        }


        // Set the RecyclerView to its corresponding view
        // Member variables for the adapter and RecyclerView
        mUpdateLocationButton = findViewById(R.id.updateLocation);
        mUpdateLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
                intent.putExtra(DetailActivity.ALARM_COORDINATES, mMapAddress);
                intent.putExtra(DetailActivity.EXTRA_ALARM_ID, mAlarmId);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(INSTANCE_MAPS_SELECTION, mMapAddress);
        super.onSaveInstanceState(outState);
    }

    public void updateData(MapAddress address){
        mMapAddress = address;
    }

    public MapAddress getData(){
        return mMapAddress;
    }
}
