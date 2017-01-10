package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

import com.satsumasoftware.timetable.db.util.SchemaUtilsKt;

/**
 * The schema for the 'timetables' table, containing constants for the column names and an SQLite
 * create statement.
 *
 * @see com.satsumasoftware.timetable.framework.Timetable
 */
public final class TimetablesSchema implements BaseColumns {

    public static final String TABLE_NAME = "timetables";
    public static final String COL_NAME = "name";
    public static final String COL_START_DATE_DAY_OF_MONTH = "start_date_day_of_month";
    public static final String COL_START_DATE_MONTH = "start_date_month";
    public static final String COL_START_DATE_YEAR = "start_date_year";
    public static final String COL_END_DATE_DAY_OF_MONTH = "end_date_day_of_month";
    public static final String COL_END_DATE_MONTH = "end_date_month";
    public static final String COL_END_DATE_YEAR = "end_date_year";
    public static final String COL_WEEK_ROTATIONS = "week_rotations";

    /**
     * An SQLite statement which creates the 'timetables' table upon execution.
     *
     * @see TimetableDbHelper
     */
    static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.PRIMARY_KEY_AUTOINCREMENT + SchemaUtilsKt.COMMA_SEP +
            COL_NAME + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_DAY_OF_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_YEAR + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_DAY_OF_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_YEAR + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_WEEK_ROTATIONS + SchemaUtilsKt.INTEGER_TYPE +
            " )";

}
