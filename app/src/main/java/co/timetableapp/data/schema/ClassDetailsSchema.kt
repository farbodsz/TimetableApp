package co.timetableapp.data.schema

import android.provider.BaseColumns

/**
 * The schema for the 'class_details' table, containing constants for the column names and an
 * SQLite create statement.
 *
 * @see co.timetableapp.framework.ClassDetail
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
     *
     * @see co.timetableapp.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_CLASS_ID + INTEGER_TYPE + COMMA_SEP +
            COL_ROOM + TEXT_TYPE + COMMA_SEP +
            COL_BUILDING + TEXT_TYPE + COMMA_SEP +
            COL_TEACHER + TEXT_TYPE +
            " )"

}
