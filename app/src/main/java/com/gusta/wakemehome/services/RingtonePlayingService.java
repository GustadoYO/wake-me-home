package com.gusta.wakemehome.services;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;

public class RingtonePlayingService extends Service {

    // Extras for ringtone uri as string and should vibrate
    public static final String EXTRA_RINGTONE_URI = "extraRingtoneUri";
    public static final String EXTRA_SHOULD_VIBRATE = "extraShouldVibrate";

    // Vibration length in milliseconds
    private static final long VIBRATION_TIME_MILLISECONDS = 500;

    private Ringtone ringtone;
    private Vibrator vibrator;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.hasExtra(EXTRA_RINGTONE_URI)) {
                Uri ringtoneUri = Uri.parse(intent.getStringExtra(EXTRA_RINGTONE_URI));

                this.ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
                ringtone.play();
            }

            if (intent.hasExtra(EXTRA_SHOULD_VIBRATE)) {
                boolean shouldVibrate =
                        intent.getBooleanExtra(EXTRA_SHOULD_VIBRATE, false);

                if (shouldVibrate) {
                    vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(VIBRATION_TIME_MILLISECONDS);
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (ringtone != null) ringtone.stop();
        if (vibrator != null) vibrator.cancel();
    }
}
