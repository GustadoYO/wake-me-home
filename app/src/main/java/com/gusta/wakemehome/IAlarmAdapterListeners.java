package com.gusta.wakemehome;

import com.gusta.wakemehome.database.AlarmEntry;

public interface IAlarmAdapterListeners {
    void onItemClickListener(int itemId);
    void onAlarmEnabledChangeListener(AlarmEntry alarm);
}
