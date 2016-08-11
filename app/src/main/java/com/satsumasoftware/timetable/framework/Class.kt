package com.satsumasoftware.timetable.framework

import android.database.Cursor
import com.satsumasoftware.timetable.db.ClassesSchema
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

class Class(val id: Int, val subjectId: Int, val day: DayOfWeek, val startTime: LocalTime,
            val endTime: LocalTime, val room: String, val teacher: String) {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_ID)),
            cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_SUBJECT_ID)),
            DayOfWeek.of(cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_DAY))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_START_TIME_MINS))),
            LocalTime.of(
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_TIME_HRS)),
                    cursor.getInt(cursor.getColumnIndex(ClassesSchema.COL_END_TIME_MINS))),
            cursor.getString(cursor.getColumnIndex(ClassesSchema.COL_ROOM)),
            cursor.getString(cursor.getColumnIndex(ClassesSchema.COL_TEACHER)))

}
