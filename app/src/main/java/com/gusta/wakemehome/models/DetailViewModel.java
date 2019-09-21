package com.gusta.wakemehome.models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

public class DetailViewModel extends ViewModel {

    private LiveData<AlarmEntry> alarm;

    DetailViewModel(AppDatabase database, int alarmId) {
        alarm = database.alarmDao().loadAlarmById(alarmId);
    }

    public LiveData<AlarmEntry> getAlarm() { return alarm; }
}
