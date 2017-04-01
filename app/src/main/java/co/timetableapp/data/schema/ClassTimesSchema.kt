package co.timetableapp.data.schema

import android.provider.BaseColumns

/**
 * The schema for the 'class_times' table, containing constants for the column names and an SQLite
 * create statement.
 *
 * @see co.timetableapp.model.ClassTime
 */
object ClassTimesSchema : BaseColumns {

    const val TABLE_NAME = "class_times"
    const val _ID = BaseColumns._ID
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_CLASS_DETAIL_ID = "class_detail_id"
    const val COL_DAY = "day"
    const val COL_WEEK_NUMBER = "week_number"
    const val COL_START_TIME_HRS = "start_time_hrs"
    const val COL_START_TIME_MINS = "start_time_mins"
    const val COL_END_TIME_HRS = "end_time_hrs"
    const val COL_END_TIME_MINS = "end_time_mins"

    /**
     * An SQLite statement which creates the 'class_times' table upon execution.
     *
     * @see co.timetableapp.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_CLASS_DETAIL_ID + INTEGER_TYPE + COMMA_SEP +
            COL_DAY + INTEGER_TYPE + COMMA_SEP +
            COL_WEEK_NUMBER + INTEGER_TYPE + COMMA_SEP +
            COL_START_TIME_HRS + INTEGER_TYPE + COMMA_SEP +
            COL_START_TIME_MINS + INTEGER_TYPE + COMMA_SEP +
            COL_END_TIME_HRS + INTEGER_TYPE + COMMA_SEP +
            COL_END_TIME_MINS + INTEGER_TYPE +
            " )"

}
