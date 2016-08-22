package com.satsumasoftware.timetable.framework

import android.database.Cursor
import com.satsumasoftware.timetable.db.TimetablesSchema
import org.threeten.bp.LocalDate

class Timetable(val id: Int, val name: String, val startDate: LocalDate, val endDate: LocalDate) {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(TimetablesSchema._ID)),
            cursor.getString(cursor.getColumnIndex(TimetablesSchema.COL_NAME)),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_START_DATE_YEAR))),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_YEAR)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(TimetablesSchema.COL_END_DATE_YEAR))))

}
