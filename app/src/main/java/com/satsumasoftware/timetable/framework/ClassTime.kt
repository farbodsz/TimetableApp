package com.satsumasoftware.timetable.framework

import android.database.Cursor
import com.satsumasoftware.timetable.db.ClassTimesSchema
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

class ClassTime(val id: Int, val day: DayOfWeek, val startTime: LocalTime, val endTime: LocalTime) {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassTimesSchema._ID)),
            DayOfWeek.of(cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_DAY))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_START_TIME_MINS))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_END_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassTimesSchema.COL_END_TIME_MINS))))

}
