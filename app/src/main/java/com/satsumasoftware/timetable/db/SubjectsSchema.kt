package com.satsumasoftware.timetable.db

import android.provider.BaseColumns
import com.satsumasoftware.timetable.db.util.COMMA_SEP
import com.satsumasoftware.timetable.db.util.INTEGER_TYPE
import com.satsumasoftware.timetable.db.util.PRIMARY_KEY_AUTOINCREMENT
import com.satsumasoftware.timetable.db.util.TEXT_TYPE

/**
 * The schema for the 'subjects' table, containing constants for the column names and an SQLite
 * create statement.

 * @see com.satsumasoftware.timetable.framework.Subject
 */
object SubjectsSchema : BaseColumns {

    const val TABLE_NAME = "subjects"
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_NAME = "name"
    const val COL_ABBREVIATION = "abbreviation"
    const val COL_COLOR_ID = "color_id"

    /**
     * An SQLite statement which creates the 'subjects' table upon execution.

     * @see TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_NAME + TEXT_TYPE + COMMA_SEP +
            COL_ABBREVIATION + TEXT_TYPE + COMMA_SEP +
            COL_COLOR_ID + INTEGER_TYPE +
            " )"

}
