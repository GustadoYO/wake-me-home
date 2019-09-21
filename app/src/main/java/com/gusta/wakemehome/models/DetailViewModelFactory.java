package com.gusta.wakemehome.models;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.gusta.wakemehome.database.AppDatabase;

public class DetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final int mAlarmId;

    public DetailViewModelFactory(AppDatabase database, int alarmId) {
        mDb = database;
        mAlarmId = alarmId;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DetailViewModel(mDb, mAlarmId);
    }
}
