package com.gusta.wakemehome;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.gusta.wakemehome.database.AppDatabase;

public class DetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mAlarmId;

    public DetailViewModelFactory(AppDatabase database, int alarmId) {
        mDb = database;
        mAlarmId = alarmId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DetailViewModel(mDb, mAlarmId);
    }
}
