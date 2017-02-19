package co.timetableapp.db.schema

import android.provider.BaseColumns

/**
 * The schema for the 'subjects' table, containing constants for the column names and an SQLite
 * create statement.
 *
 * @see co.timetableapp.framework.Subject
 */
object SubjectsSchema : BaseColumns {

    const val TABLE_NAME = "subjects"
    const val _ID = BaseColumns._ID
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_NAME = "name"
    const val COL_ABBREVIATION = "abbreviation"
    const val COL_COLOR_ID = "color_id"

    /**
     * An SQLite statement which creates the 'subjects' table upon execution.
     *
     * @see co.timetableapp.db.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_NAME + TEXT_TYPE + COMMA_SEP +
            COL_ABBREVIATION + TEXT_TYPE + COMMA_SEP +
            COL_COLOR_ID + INTEGER_TYPE +
            " )"

}
