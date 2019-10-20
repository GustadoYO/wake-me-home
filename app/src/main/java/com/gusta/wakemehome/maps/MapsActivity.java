package com.gusta.wakemehome.maps;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gusta.wakemehome.DetailActivity;
import com.gusta.wakemehome.R;

import static com.gusta.wakemehome.DetailActivity.EXTRA_ALARM_COORDINATES;


public class MapsActivity extends AppCompatActivity{

    private static final String TAG = MapsActivity.class.getSimpleName();
    public static final String INSTANCE_MAPS_ADDRESS_DATA = "instanceMapsAddressData";

    private Button mUpdateLocationButton;
    private MapProvider mMapProvider;
    private TextView mRadiusText;
    private SeekBar mRadiusSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Init the data binding object
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRadiusSlider = findViewById(R.id.radius_slider);
        mRadiusText = findViewById(R.id.seekBarInfoTextView);
        mMapProvider = new GoogleMaps(this);

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MAPS_ADDRESS_DATA)) {
            MapAddress address = savedInstanceState.getParcelable(INSTANCE_MAPS_ADDRESS_DATA);
            mMapProvider.setMapAddress(address);
            float radius  = address.getRadius();
            mRadiusSlider.setProgress((int)radius);
            mRadiusText.setText(Float.toString(radius));
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ALARM_COORDINATES)) {
            MapAddress address = intent.getParcelableExtra(EXTRA_ALARM_COORDINATES);
            mMapProvider.setMapAddress(address);

            float radius  = address.getRadius();
            mRadiusSlider.setProgress((int)radius);
            mRadiusText.setText(Float.toString(radius));
        }


        mRadiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeRadius(seekBar);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        mUpdateLocationButton = findViewById(R.id.updateLocation);
        mUpdateLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MapAddress address = mMapProvider.getMapAddress();
                if(address != null && address.isValidEntry()) {
                    Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_ALARM_COORDINATES, address);
                    setResult(1,intent );
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),R.string.error_mandatory,Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(INSTANCE_MAPS_ADDRESS_DATA, mMapProvider.getMapAddress());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
    private void changeRadius(SeekBar seekBar){
        float radius = seekBar.getProgress();
        mRadiusText.setText(Float.toString(radius));
        mMapProvider.updateSelectedLocation(radius);
    }
}
