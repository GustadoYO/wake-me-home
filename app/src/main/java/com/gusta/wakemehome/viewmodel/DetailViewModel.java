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

public class DetailViewModel extends ViewModel {

    private LiveData<AlarmEntry> mAlarm;
    private AppDatabase mDb;

    public DetailViewModel(AppDatabase db,int alarmId) {
        mDb = db;
        mAlarm = mDb.alarmDao().loadAlarmById(alarmId);
    }

    public LiveData<AlarmEntry> getAlarm() {
        return mAlarm;
    }

    //TODO use app executor
    public int insertAlarm(AlarmEntry alarm) {
        return (int)mDb.alarmDao().insertAlarm(alarm);
    }
    //TODO use app executor
    public void updateAlarm(AlarmEntry alarm) {
        mDb.alarmDao().updateAlarm(alarm);
    }

}
