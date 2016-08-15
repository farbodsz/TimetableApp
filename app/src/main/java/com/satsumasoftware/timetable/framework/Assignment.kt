package com.satsumasoftware.timetable.framework

import android.database.Cursor
import com.satsumasoftware.timetable.db.AssignmentsSchema
import org.threeten.bp.LocalDate

class Assignment(val id: Int, val classId: Int, val title: String, val detail: String,
                 val dueDate: LocalDate, val completionProgress: Int) {

    constructor(cursor: Cursor) : this(
            cursor.getInt(cursor.getColumnIndex(AssignmentsSchema._ID)),
            cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_CLASS_ID)),
            cursor.getString(cursor.getColumnIndex(AssignmentsSchema.COL_TITLE)),
            cursor.getString(cursor.getColumnIndex(AssignmentsSchema.COL_DETAIL)),
            LocalDate.of(
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_DAY_OF_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_MONTH)),
                    cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_DUE_DATE_YEAR))),
            cursor.getInt(cursor.getColumnIndex(AssignmentsSchema.COL_COMPLETION_PROGRESS)))

}
