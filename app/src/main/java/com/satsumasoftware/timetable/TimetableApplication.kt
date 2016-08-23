package com.satsumasoftware.timetable

import android.app.Application

import com.jakewharton.threetenabp.AndroidThreeTen
import com.satsumasoftware.timetable.framework.Timetable

class TimetableApplication : Application() {

    var currentTimetable: Timetable? = null

    override fun onCreate() {
        super.onCreate()

        AndroidThreeTen.init(this)
    }
}
