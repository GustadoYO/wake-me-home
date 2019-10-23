package com.gusta.wakemehome.viewmodel;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.gusta.wakemehome.database.AppDatabase;

public class DetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final int mAlarmId;
    private final AppDatabase mDb;

    public DetailViewModelFactory(AppDatabase db, int alarmId) {
        mAlarmId = alarmId;
        mDb = db;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        //noinspection unchecked
        return (T) new DetailViewModel(mDb,mAlarmId);
    }
}