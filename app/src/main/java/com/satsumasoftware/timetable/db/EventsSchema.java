package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

import com.satsumasoftware.timetable.db.util.SchemaUtilsKt;

/**
 * The schema for the 'events' table, containing constants for the column names and an SQLite create
 * statement.
 *
 * @see com.satsumasoftware.timetable.framework.Event
 */
public final class EventsSchema implements BaseColumns {

    public static final String TABLE_NAME = "events";
    public static final String COL_TIMETABLE_ID = "timetable_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DETAIL = "detail";
    public static final String COL_START_DATE_DAY_OF_MONTH = "start_date_day_of_month";
    public static final String COL_START_DATE_MONTH = "start_date_month";
    public static final String COL_START_DATE_YEAR = "start_date_year";
    public static final String COL_START_TIME_HRS = "start_time_hrs";
    public static final String COL_START_TIME_MINS = "start_time_mins";
    public static final String COL_END_DATE_DAY_OF_MONTH = "end_date_day_of_month";
    public static final String COL_END_DATE_MONTH = "end_date_month";
    public static final String COL_END_DATE_YEAR = "end_date_year";
    public static final String COL_END_TIME_HRS = "end_time_hrs";
    public static final String COL_END_TIME_MINS = "end_time_mins";

    /**
     * An SQLite statement which creates the 'events' table upon execution.
     *
     * @see TimetableDbHelper
     */
    static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.PRIMARY_KEY_AUTOINCREMENT + SchemaUtilsKt.COMMA_SEP +
            COL_TIMETABLE_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_TITLE + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DETAIL + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_DAY_OF_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_YEAR + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_TIME_HRS + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_TIME_MINS + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_DAY_OF_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_YEAR + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_TIME_HRS + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_TIME_MINS + SchemaUtilsKt.INTEGER_TYPE +
            " )";

}
