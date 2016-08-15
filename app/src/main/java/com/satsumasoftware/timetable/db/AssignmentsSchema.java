package com.satsumasoftware.timetable.db;

import android.provider.BaseColumns;

public final class AssignmentsSchema implements BaseColumns {

    public static final String TABLE_NAME = "assignments";
    public static final String COL_CLASS_ID = "class_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DETAIL = "detail";
    public static final String COL_DUE_DATE_HRS = "due_date_hrs";
    public static final String COL_DUE_DATE_MINS = "due_date_mins";
    public static final String COL_COMPLETION_PROGRESS = "completion_progress";

    protected static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + "( " +
            _ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_CLASS_ID + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_TITLE + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DETAIL + SchemaUtilsKt.TEXT_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DUE_DATE_HRS + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_DUE_DATE_MINS + SchemaUtilsKt.INTEGER_TYPE + SchemaUtilsKt.COMMA_SEP +
            COL_COMPLETION_PROGRESS + SchemaUtilsKt.INTEGER_TYPE +
            " )";

    protected static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
