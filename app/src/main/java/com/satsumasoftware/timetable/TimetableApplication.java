package com.satsumasoftware.timetable;

import android.app.Application;

import com.jakewharton.threetenabp.AndroidThreeTen;

public class TimetableApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidThreeTen.init(this);
    }
}
