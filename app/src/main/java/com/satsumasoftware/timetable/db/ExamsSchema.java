package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

import com.satsumasoftware.timetable.db.util.SchemaUtilsKt;

/**
 * The schema for the 'exams' table, containing constants for the column names and an SQLite create
 * statement.
 *
 * @see com.satsumasoftware.timetable.framework.Exam
 */
public final class ExamsSchema implements BaseColumns {

    public static final String TABLE_NAME = "exams";
    public static final String COL_TIMETABLE_ID = "timetable_id";
    public static final String COL_SUBJECT_ID = "subject_id";
    public static final String COL_MODULE = "module";
    public static final String COL_DATE_DAY_OF_MONTH = "date_day_of_month";
    public static final String COL_DATE_MONTH = "date_month";
    public static final String COL_DATE_YEAR = "date_year";
    public static final String COL_START_TIME_HRS = "start_time_hrs";
    public static final String COL_START_TIME_MINS = "start_time_mins";
    public static final String COL_DURATION = "duration";
    public static final String COL_SEAT = "seat";
    public static final String COL_ROOM = "room";
    public static final String COL_IS_RESIT = "is_resit";

    /**
     * An SQLite statement which creates the 'exams' table upon execution.
     *
     * @see TimetableDbHelper
     */
    static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.PRIMARY_KEY_AUTOINCREMENT + SchemaUtilsKt.COMMA_SEP +
            COL_TIMETABLE_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_SUBJECT_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_MODULE + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DATE_DAY_OF_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DATE_MONTH + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DATE_YEAR + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_TIME_HRS + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_START_TIME_MINS + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DURATION + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_SEAT + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_ROOM + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_IS_RESIT + SchemaUtilsKt.INTEGER_TYPE +
            " )";

}
