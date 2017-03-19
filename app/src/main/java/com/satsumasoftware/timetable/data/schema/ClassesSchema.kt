package com.satsumasoftware.timetable.data.schema

import android.provider.BaseColumns

/**
 * The schema for the 'classes' table, containing constants for the column names and an SQLite
 * create statement.
 *
 * @see com.satsumasoftware.timetable.framework.Class
 */
object ClassesSchema : BaseColumns {

    const val TABLE_NAME = "classes"
    const val _ID = BaseColumns._ID
    const val COL_TIMETABLE_ID = "timetable_id"
    const val COL_SUBJECT_ID = "subject_id"
    const val COL_MODULE_NAME = "module_name"
    const val COL_START_DATE_DAY_OF_MONTH = "start_date_day_of_month"
    const val COL_START_DATE_MONTH = "start_date_month"
    const val COL_START_DATE_YEAR = "start_date_year"
    const val COL_END_DATE_DAY_OF_MONTH = "end_date_day_of_month"
    const val COL_END_DATE_MONTH = "end_date_month"
    const val COL_END_DATE_YEAR = "end_date_year"

    /**
     * An SQLite statement which creates the 'classes' table upon execution.
     *
     * @see com.satsumasoftware.timetable.data.TimetableDbHelper
     */
    internal const val SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            BaseColumns._ID + INTEGER_TYPE + COMMA_SEP +
            COL_TIMETABLE_ID + INTEGER_TYPE + COMMA_SEP +
            COL_SUBJECT_ID + INTEGER_TYPE + COMMA_SEP +
            COL_MODULE_NAME + TEXT_TYPE + COMMA_SEP +
            COL_START_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_START_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_START_DATE_YEAR + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_MONTH + INTEGER_TYPE + COMMA_SEP +
            COL_END_DATE_YEAR + INTEGER_TYPE +
            " )"

}
