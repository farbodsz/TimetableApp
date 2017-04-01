package co.timetableapp.data.schema

import android.provider.BaseColumns

/**
 * The schema for the 'timetables' table, containing constants for the column names and an SQLite
 * create statement.
 *
 * @see co.timetableapp.model.Timetable
 */
object TimetablesSchema : BaseColumns {

    const val TABLE_NAME = "timetables"
    const val _ID = BaseColumns._ID
    const val COL_NAME = "name"
    const val COL_START_DATE_DAY_OF_MONTH = "start_date_day_of_month"
    const val COL_START_DATE_MONTH = "start_date_month"
    const val COL_START_DATE_YEAR = "start_date_year"
    const val COL_END_DATE_DAY_OF_MONTH = "end_date_day_of_month"
    const val COL_END_DATE_MONTH = "end_date_month"
    const val COL_END_DATE_YEAR = "end_date_year"
    const val COL_WEEK_ROTATIONS = "week_rotations"

    /**
     * An SQLite statement which creates the 'timetables' table upon execution.
     *
     * @see co.timetableapp.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_NAME + TEXT_TYPE + COMMA_SEP +
            COL_START_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_START_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_START_DATE_YEAR + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_YEAR + INTEGER_TYPE + COMMA_SEP +
            COL_WEEK_ROTATIONS + INTEGER_TYPE +
            " )"

}
