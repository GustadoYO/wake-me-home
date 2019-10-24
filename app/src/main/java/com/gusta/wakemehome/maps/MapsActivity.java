package com.gusta.wakemehome.maps;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gusta.wakemehome.DetailActivity;
import com.gusta.wakemehome.R;
import com.gusta.wakemehome.utilities.UnitsUtils;

import static com.gusta.wakemehome.DetailActivity.EXTRA_ALARM_ADDRESS;


public class MapsActivity extends AppCompatActivity {

    private static final String TAG = MapsActivity.class.getSimpleName();
    public static final String INSTANCE_MAPS_ADDRESS_DATA = "instanceMapsAddressData";

    private Button mUpdateLocationButton;
    private MapProvider mMapProvider;
    private TextView mRadiusText;
    private SeekBar mRadiusSlider;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRadiusSlider = findViewById(R.id.radius_slider);
        mRadiusText = findViewById(R.id.seekBarInfoTextView);
        mMapProvider = new GoogleMapsProvider(this);

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_MAPS_ADDRESS_DATA)) {
            MapAddress address = savedInstanceState.getParcelable(INSTANCE_MAPS_ADDRESS_DATA);
            mMapProvider.setMapAddress(address);
            float radius = address.getRadius();
            mRadiusSlider.setProgress((int) radius);
            mRadiusText.setText(UnitsUtils.formatLength(this, radius));
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_ALARM_ADDRESS)) {
            MapAddress address = intent.getParcelableExtra(EXTRA_ALARM_ADDRESS);
            mMapProvider.setMapAddress(address);

            float radius = address.getRadius();
            mRadiusSlider.setProgress((int) radius);
            mRadiusText.setText(UnitsUtils.formatLength(this, radius));
        }


        mRadiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeRadius(seekBar);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mUpdateLocationButton = findViewById(R.id.updateLocation);
        mUpdateLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MapAddress address = mMapProvider.getMapAddress();
                if (address != null && address.isValidEntry()) {
                    Intent intent = new Intent(MapsActivity.this, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_ALARM_ADDRESS, address);
                    setResult(1, intent);
                    finish();
                } else {
                    if(toast != null)
                        toast.cancel();
                    toast = Toast.makeText(getApplicationContext(), R.string.error_mandatory, Toast.LENGTH_SHORT);
                    toast.show();
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
        boolean isOptionSelected = super.onOptionsItemSelected(item);
        finish();
        return isOptionSelected;
    }

    private void changeRadius(SeekBar seekBar) {
        //if address doesn't exist
        if (mMapProvider.getMapAddress() == null) {
            //keep toast to avoid multiple toasts on the screen
            if(toast != null)
                toast.cancel();
            //create toast for radius without selection on map
            toast = Toast.makeText(getApplicationContext(), R.string.selection_order, Toast.LENGTH_SHORT);
            toast.show();
            seekBar.setProgress(0);
            return;
        }
        toast = null;
        float radius = seekBar.getProgress();
        mRadiusText.setText(UnitsUtils.formatLength(this, radius));
        mMapProvider.updateRadius(radius);
    }

}
