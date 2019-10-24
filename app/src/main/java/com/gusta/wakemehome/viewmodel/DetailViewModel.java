package com.gusta.wakemehome.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

public class DetailViewModel extends ViewModel {

    private LiveData<AlarmEntry> mAlarm;
    private AppDatabase mDb;

    public DetailViewModel(AppDatabase db, int alarmId) {
        mDb = db;
        mAlarm = mDb.alarmDao().loadAlarmById(alarmId);
    }

    public LiveData<AlarmEntry> getAlarm() {
        return mAlarm;
    }

}
