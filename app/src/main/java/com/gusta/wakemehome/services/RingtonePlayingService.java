package com.gusta.wakemehome.services;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;

public class RingtonePlayingService extends Service {

    // Extra for ringtone uri as string
    public static final String EXTRA_RINGTONE_URI = "extraRingtoneUri";

    private Ringtone ringtone;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_RINGTONE_URI)) {
            Uri ringtoneUri = Uri.parse(intent.getStringExtra(EXTRA_RINGTONE_URI));

            this.ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            ringtone.play();
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        ringtone.stop();
    }
}
