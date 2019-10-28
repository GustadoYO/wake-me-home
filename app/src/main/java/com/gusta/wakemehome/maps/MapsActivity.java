package com.gusta.wakemehome.maps;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.gusta.wakemehome.R;
import com.gusta.wakemehome.utilities.Constants;
import com.gusta.wakemehome.utilities.FileUtils;
import com.gusta.wakemehome.utilities.UnitsUtils;


public class MapsActivity extends AppCompatActivity {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String INSTANCE_MAPS_ADDRESS_DATA = "instanceMapsAddressData";

    private MapProvider mMapProvider;
    private TextView mRadiusText;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        SeekBar radiusSlider = findViewById(R.id.radius_slider);
        mRadiusText = findViewById(R.id.seekBarInfoTextView);
        mMapProvider = new GoogleMapsProvider(this);

        // Check for saved state (like after phone orientation change) - and load it
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(INSTANCE_MAPS_ADDRESS_DATA)) {
            MapDestination address = savedInstanceState.getParcelable(INSTANCE_MAPS_ADDRESS_DATA);
            assert address != null;
            mMapProvider.setMapDestination(address);
            float radius = address.getRadius();
            radiusSlider.setProgress((int) radius);
            mRadiusText.setText(UnitsUtils.formatLength(this, radius));
        }
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Constants.EXTRA_ALARM_DESTINATION)) {
            MapDestination address = intent.getParcelableExtra(Constants.EXTRA_ALARM_DESTINATION);
            mMapProvider.setMapDestination(address);

            float radius = address.getRadius();
            radiusSlider.setProgress((int) radius);
            mRadiusText.setText(UnitsUtils.formatLength(this, radius));
        }


        radiusSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                changeRadius(seekBar);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Button updateLocationButton = findViewById(R.id.updateLocation);
        updateLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MapDestination address = mMapProvider.getMapDestination();
                if (address != null && address.getRadius() > 0) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.EXTRA_ALARM_DESTINATION, address);
                    setResult(1, intent);
                    finish();
                } else {
                    if(toast != null)
                        toast.cancel();
                    toast = Toast.makeText(getApplicationContext(),
                            R.string.error_mandatory,
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(INSTANCE_MAPS_ADDRESS_DATA, mMapProvider.getMapDestination());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isOptionSelected = super.onOptionsItemSelected(item);
        FileUtils.deleteTempImage();
        finish();
        return isOptionSelected;
    }

    private void changeRadius(SeekBar seekBar) {
        //if address doesn't exist
        if (mMapProvider.getMapDestination() == null) {
            //keep toast to avoid multiple toasts on the screen
            if(toast != null)
                toast.cancel();
            //create toast for radius without selection on map
            toast = Toast.makeText(getApplicationContext(),
                    R.string.selection_order,
                    Toast.LENGTH_SHORT);
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
