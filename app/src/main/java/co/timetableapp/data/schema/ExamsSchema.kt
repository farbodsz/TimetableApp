package co.timetableapp.data.schema

import android.provider.BaseColumns

/**
 * The schema for the 'exams' table, containing constants for the column names and an SQLite create
 * statement.
 *
 * @see co.timetableapp.model.Exam
 */
object ExamsSchema : BaseColumns {

    const val TABLE_NAME = "exams"
    const val _ID = BaseColumns._ID
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_SUBJECT_ID = "subject_id"
    const val COL_MODULE = "module"
    const val COL_DATE_DAY_OF_MONTH = "date_day_of_month"
    const val COL_DATE_MONTH = "date_month"
    const val COL_DATE_YEAR = "date_year"
    const val COL_START_TIME_HRS = "start_time_hrs"
    const val COL_START_TIME_MINS = "start_time_mins"
    const val COL_DURATION = "duration"
    const val COL_SEAT = "seat"
    const val COL_ROOM = "room"
    const val COL_IS_RESIT = "is_resit"

    /**
     * An SQLite statement which creates the 'exams' table upon execution.
     *
     * @see co.timetableapp.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + PRIMARY_KEY_AUTOINCREMENT + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_SUBJECT_ID + INTEGER_TYPE + COMMA_SEP +
            COL_MODULE + TEXT_TYPE + COMMA_SEP +
            COL_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_DATE_YEAR + INTEGER_TYPE + COMMA_SEP +
            COL_START_TIME_HRS + INTEGER_TYPE + COMMA_SEP +
            COL_START_TIME_MINS + INTEGER_TYPE + COMMA_SEP +
            COL_DURATION + INTEGER_TYPE + COMMA_SEP +
            COL_SEAT + TEXT_TYPE + COMMA_SEP +
            COL_ROOM + TEXT_TYPE + COMMA_SEP +
            COL_IS_RESIT + INTEGER_TYPE +
            " )"

}
