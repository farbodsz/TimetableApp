package com.satsumasoftware.timetable.db

import android.provider.BaseColumns
import com.satsumasoftware.timetable.db.util.COMMA_SEP
import com.satsumasoftware.timetable.db.util.INTEGER_TYPE
import com.satsumasoftware.timetable.db.util.PRIMARY_KEY_AUTOINCREMENT
import com.satsumasoftware.timetable.db.util.TEXT_TYPE

/**
 * The schema for the 'assignments' table, containing constants for the column names and the SQLite
 * create statement.

 * @see com.satsumasoftware.timetable.framework.Assignment
 */
object AssignmentsSchema : BaseColumns {

    const val TABLE_NAME = "assignments"
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_CLASS_ID = "class_id"
    const val COL_TITLE = "title"
    const val COL_DETAIL = "detail"
    const val COL_DUE_DATE_DAY_OF_MONTH = "due_date_day_of_month"
    const val COL_DUE_DATE_MONTH = "due_date_month"
    const val COL_DUE_DATE_YEAR = "due_date_year"
    const val COL_COMPLETION_PROGRESS = "completion_progress"

    /**
     * An SQLite statement which creates the 'assignments' table upon execution.

     * @see TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_CLASS_ID + INTEGER_TYPE + COMMA_SEP +
            COL_TITLE + TEXT_TYPE + COMMA_SEP +
            COL_DETAIL + TEXT_TYPE + COMMA_SEP +
            COL_DUE_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_DUE_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_DUE_DATE_YEAR + INTEGER_TYPE + COMMA_SEP +
            COL_COMPLETION_PROGRESS + INTEGER_TYPE +
            " )"

}
