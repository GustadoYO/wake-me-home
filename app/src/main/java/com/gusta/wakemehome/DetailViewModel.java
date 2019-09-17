package com.gusta.wakemehome;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

class DetailViewModel extends ViewModel {

    private LiveData<AlarmEntry> alarm;

    public DetailViewModel(AppDatabase database, int alarmId) {
        alarm = database.alarmDao().loadAlarmById(alarmId);
    }

    public LiveData<AlarmEntry> getAlarm() { return alarm; }
}
