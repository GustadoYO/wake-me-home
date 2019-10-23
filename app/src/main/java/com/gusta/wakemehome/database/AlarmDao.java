package com.gusta.wakemehome.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlarmDao {

    @Query("SELECT * FROM alarm ORDER BY id")
    LiveData<List<AlarmEntry>> loadAllAlarms();

    @Insert
    long insertAlarm(AlarmEntry alarmEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAlarm(AlarmEntry alarmEntry);

    @Delete
    void deleteAlarm(AlarmEntry alarmEntry);

    @Query("SELECT * FROM alarm WHERE id = :id")
    LiveData<AlarmEntry> loadAlarmById(int id);
}
