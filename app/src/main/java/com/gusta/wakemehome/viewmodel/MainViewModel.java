package com.gusta.wakemehome.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

import java.io.File;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    //===========//
    // CONSTANTS //
    //===========//

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    //=========//
    // MEMBERS //
    //=========//

    private LiveData<List<AlarmEntry>> alarms;
    private AppDatabase database;

    //===================//
    // GETTERS & SETTERS //
    //===================//

    public LiveData<List<AlarmEntry>> getAlarms() {
        return alarms;
    }

    //=========//
    // METHODS //
    //=========//

    public MainViewModel(@NonNull Application application) {
        super(application);

        // Retrieve the alarms list from the db into a LiveData object
        database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the alarms from the DataBase");
        alarms = database.alarmDao().loadAllAlarms();
    }

    //update enable only on main view model
    public void updateAlarm(AlarmEntry alarm){
        database.alarmDao().updateAlarm(alarm);
    }

    //delete file is not part of db but it's a part of data presentation
    //that's the reason why to use it inside the view model
    public void deleteAlarm(AlarmEntry alarm){
        database.alarmDao().deleteAlarm(alarm);

        //TODO: Move it to utils
        //delete images for deleted alarms
        File file = new File(getImagePath(alarm));

        if (file.exists()) {
            if (!file.delete()) {
                //TODO handle error
                Log.w(TAG,"image delete failed");
            }
        }
    }

    //TODO: Move it to utils
    private String getLocalMapDir(){
        ContextWrapper cw = new ContextWrapper(getApplication().getApplicationContext());

        File directory = cw.getDir("mapsDir", Context.MODE_PRIVATE);
        return directory.getAbsolutePath();
    }

    //TODO: Move it to utils
    private String getImagePath(AlarmEntry alarm){
        return getLocalMapDir() + "/" + alarm.getId() + ".png";
    }

}
