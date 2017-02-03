package com.satsumasoftware.timetable.db

import android.provider.BaseColumns
import com.satsumasoftware.timetable.db.util.COMMA_SEP
import com.satsumasoftware.timetable.db.util.INTEGER_TYPE
import com.satsumasoftware.timetable.db.util.PRIMARY_KEY_AUTOINCREMENT
import com.satsumasoftware.timetable.db.util.TEXT_TYPE

/**
 * The schema for the 'class_details' table, containing constants for the column names and an
 * SQLite create statement.

 * @see com.satsumasoftware.timetable.framework.ClassDetail
 */
object ClassDetailsSchema : BaseColumns {

    const val TABLE_NAME = "class_details"
    const val _ID = BaseColumns._ID
    const val COL_CLASS_ID = "class_id"
    const val COL_ROOM = "room"
    const val COL_BUILDING = "building"
    const val COL_TEACHER = "teacher"

    /**
     * An SQLite statement which creates the 'class_details' table upon execution.

     * @see TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_CLASS_ID + INTEGER_TYPE + COMMA_SEP +
            COL_ROOM + TEXT_TYPE + COMMA_SEP +
            COL_BUILDING + TEXT_TYPE + COMMA_SEP +
            COL_TEACHER + TEXT_TYPE +
            " )"

}
