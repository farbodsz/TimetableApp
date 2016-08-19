package com.satsumasoftware.timetable

import android.app.Application

import com.jakewharton.threetenabp.AndroidThreeTen

class TimetableApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
    }
}
