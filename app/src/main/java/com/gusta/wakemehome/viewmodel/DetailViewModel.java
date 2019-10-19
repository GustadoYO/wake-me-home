package com.gusta.wakemehome.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.gusta.wakemehome.database.AlarmEntry;
import com.gusta.wakemehome.database.AppDatabase;

public class DetailViewModel extends AndroidViewModel {

    private LiveData<AlarmEntry> mAlarm;
    private AppDatabase database;

    public DetailViewModel(@NonNull Application application) {
        super(application);
        database = AppDatabase.getInstance(this.getApplication());
    }

    public LiveData<AlarmEntry> getAlarm() {
        return mAlarm;
    }
    public LiveData<AlarmEntry> getAlarm(int alarmId) {
        mAlarm = database.alarmDao().loadAlarmById(alarmId);
        return mAlarm;
    }
    public void insertAlarm(AlarmEntry alarm) {
        //clear default
        alarm.setId(0);
        database.alarmDao().insertAlarm(alarm);
    }
    public void updateAlarm(AlarmEntry alarm) {
        database.alarmDao().updateAlarm(alarm);
    }
}
