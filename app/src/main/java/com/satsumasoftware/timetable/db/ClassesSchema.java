package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

import com.satsumasoftware.timetable.db.util.SchemaUtilsKt;

/**
 * The schema for the 'classes' table, containing constants for the column names and an SQLite
 * create statement.
 *
 * @see com.satsumasoftware.timetable.framework.Class
 */
public final class ClassesSchema implements BaseColumns {

    public static final String TABLE_NAME = "classes";
    public static final String COL_TIMETABLE_ID = "timetable_id";
    public static final String COL_SUBJECT_ID = "subject_id";
    public static final String COL_MODULE_NAME = "module_name";
    public static final String COL_START_DATE_DAY_OF_MONTH = "start_date_day_of_month";
    public static final String COL_START_DATE_MONTH = "start_date_month";
    public static final String COL_START_DATE_YEAR = "start_date_year";
    public static final String COL_END_DATE_DAY_OF_MONTH = "end_date_day_of_month";
    public static final String COL_END_DATE_MONTH = "end_date_month";
    public static final String COL_END_DATE_YEAR = "end_date_year";

    /**
     * An SQLite statement which creates the 'classes' table upon execution.
     *
     * @see TimetableDbHelper
     */
    static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_TIMETABLE_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_SUBJECT_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_MODULE_NAME + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_DAY_OF_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_DATE_YEAR + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_DAY_OF_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_END_DATE_YEAR + SchemaUtilsKt.INTEGER_TYPE +
            " )";

}
