package com.gusta.wakemehome.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

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
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the alarms from the DataBase");
        alarms = database.alarmDao().loadAllAlarms();
    }
}
