package com.gusta.wakemehome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private String mAlarm;
    private TextView mAlarmDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mAlarmDisplay = (TextView) findViewById(R.id.tv_display_alarm);

        Intent intentThatStartedThisActivity = getIntent();

        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_TEXT)) {
                mAlarm = intentThatStartedThisActivity.getStringExtra(Intent.EXTRA_TEXT);
                mAlarmDisplay.setText(mAlarm);
            }
        }
    }
}
