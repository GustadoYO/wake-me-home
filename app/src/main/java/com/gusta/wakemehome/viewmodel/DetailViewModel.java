package com.gusta.wakemehome.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

public class DetailViewModel extends ViewModel {

    private LiveData<AlarmEntry> alarm;

    DetailViewModel(AppDatabase database, int alarmId) {
        alarm = database.alarmDao().loadAlarmById(alarmId);
    }

    public LiveData<AlarmEntry> getAlarm() { return alarm; }
}
